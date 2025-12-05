package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Bind;

import org.aksw.commons.util.docker.ContainerPathResolver;
import org.aksw.commons.util.docker.ContainerUtils;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgWord;
import org.aksw.shellgebra.algebra.cmd.arg.Token;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenPath;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.algebra.cmd.op.CmdOps;
import org.aksw.shellgebra.algebra.cmd.redirect.CmdRedirect;
import org.aksw.shellgebra.algebra.cmd.transform.CmdString;
import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.exec.graph.JRedirect;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectJava;
import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.aksw.vshell.shim.rdfconvert.ArgumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process builder that starts a process in a docker container via docker run.
 */
public class ProcessBuilderDocker
    extends ProcessBuilderBase<ProcessBuilderDocker>
{
    private static final Logger logger = LoggerFactory.getLogger(ProcessBuilderDocker.class);

    // private GenericContainer<?> container;
    protected String imageRef;
    protected String entrypoint;

    // protected String imageTag;
    // protected CmdOp cmdOp;
    protected String workingDirectory;

    protected ContainerPathResolver containerPathResolver;
    protected FileMapper fileMapper;

    protected Function<CmdOpVar, Stage> varResolver;
    protected List<Bind> binds;

    protected boolean interactive;

    public ProcessBuilderDocker() {
        super();
    }

    public static ProcessBuilderDocker of(String ... command) {
        return new ProcessBuilderDocker().command(command);
    }

    public String imageRef() {
        return imageRef;
    }

    public ProcessBuilderDocker imageRef(String imageRef) {
        this.imageRef = imageRef;
        return self();
    }

    public String entrypoint() {
        return entrypoint;
    }

    public ProcessBuilderDocker interactive(boolean onOrOff) {
        this.interactive = onOrOff;
        return self();
    }

    public boolean interactive() {
        return interactive;
    }

    /**
     * Set an explicit entrypoint.
     * If none is set, an attempt to infer one from the context will be made upon starting the process.
     *
     * @param entrypoint
     * @return
     */
    public ProcessBuilderDocker entrypoint(String entrypoint) {
        this.entrypoint = entrypoint;
        return self();
    }

    public FileMapper fileMapper() {
        return fileMapper;
    }

    public ProcessBuilderDocker fileMapper(FileMapper fileMapper) {
        this.fileMapper = fileMapper;
        return self();
    }

    public String workingDirectory() {
        return workingDirectory;
    }

    public ProcessBuilderDocker workingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
        return self();
    }

    @Override
    public Process start(ProcessRunner executor) throws IOException {
        return execInternal(executor);
    }

    // TODO We need to set up a helper cat in-pipe-end > named-pipe
    protected Process catInputProcess(Path pipeReadEnd, Path namedPipePath) throws IOException {
        CmdOpExec cat = new CmdOpExec(List.of(), "cat", ArgumentList.of(
            CmdArg.ofPathString(pipeReadEnd.toString()),
            CmdArg.redirect(CmdRedirect.out(namedPipePath.toString()))));
        String scriptString = toScriptString(cat);

        ProcessBuilder pb = new ProcessBuilder("bash", "-c", scriptString);
        Process process = pb.start();
        return process;
    }

    // protected Process execToPathInternal(Path outPath, String outContainerPath, PathLifeCycle pathLifeCycle) {
    protected Process execInternal(ProcessRunner executor) throws IOException {
        // Closer closer = Closer.create();
        List<FileWriterTask> inputTasks = new ArrayList<>();

        // Map the pipes into the container
//        Path hostOutputPipe = executor.outputPipe();
//        Path hostErrorPipe = executor.errorPipe();


//        Process pumpIn;
//        try {
//            pumpIn = catInputProcess(hostInputPipe);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        Path inputPath = null;
        boolean isInputPathMountable = false;

        Path hostMountableInputPath = null;

        JRedirect inRedirect = redirectInput();
        if (inRedirect instanceof JRedirectJava x) {
            Redirect r = x.redirect();
            switch (r.type()) {
            case INHERIT:
                inputPath = executor.inputPipe();
                isInputPathMountable = false;
                break;
            case READ:
                hostMountableInputPath = r.file().toPath();
                isInputPathMountable = true;
                break;
            default:
                throw new RuntimeException("Unsupported or not implemented yet.");
            }
        }

        if (!isInputPathMountable) {
            Path namedPipePath = Path.of("/tmp/named-pipe-" + System.nanoTime());
            SysRuntime.newNamedPipe(namedPipePath);
            hostMountableInputPath = namedPipePath;
            Process pumpProcess = catInputProcess(inputPath, hostMountableInputPath);
        }




        FileMapper finalFileMapper = fileMapper.clone();
        String containerInputPath = finalFileMapper.allocate(hostMountableInputPath.toString(), AccessMode.ro);
//        String containerOutputPath = finalFileMapper.allocate(hostOutputPipe.toString(), AccessMode.rw);
//        String containerErrorPath = finalFileMapper.allocate(hostErrorPipe.toString(), AccessMode.rw);

        CmdOp op = CmdOpExec.ofLiteralArgs(super.command().toArray(String[]::new));
        op = CmdOps.appendRedirects(op,
                CmdRedirect.in(containerInputPath)
                // CmdRedirect.out(containerOutputPath),
                // CmdRedirect.err(containerErrorPath)
                );

        // If there is a byte source as a file writer then start it.
        org.testcontainers.containers.GenericContainer<?> container;
        try {
            container = setupContainer(op, finalFileMapper);
            container.start();
            return new ProcessOverDockerContainer(container);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected String getUserString() throws IOException {
        return ContainerUtils.getUserString();
    }

    public static String toScriptString(CmdOp cmdOp) {
        SysRuntime runtime = SysRuntimeImpl.forCurrentOs();
        CmdOp dummy = new CmdOpExec("/dummy", CmdArg.ofCommandSubstitution(cmdOp));
        CmdString cmdString = runtime.compileString(dummy);
        String scriptString = cmdString.cmd()[1];
        return scriptString;
    }

    protected org.testcontainers.containers.GenericContainer<?> setupContainer(CmdOp cmdOp, FileMapper fileMapper) throws IOException {
        // TODO Consolidate with SysRuntimeCore: Need to get the appropriate bash entry point from some registry.

        String userStr = getUserString();
        logger.info("Setting up container " + imageRef + " with UID:GID=" + userStr);

        String scriptString = toScriptString(cmdOp);

        // String[] entrypoint = new String[]{"bash"};
        String[] cmdParts;
        if (true) { // cmdString.isScriptString()) {
            cmdParts = new String[] {
                "-c",
                // List.of(cmdString.cmd()).stream().collect(Collectors.joining(" "))
                scriptString
            };
        } else {
//            CmdStrOps strOps = runtime.getStrOps();
//            cmdParts = cmdString.cmd(); // XXX Must ensure that the command is resolvable!
        }

        List.of(cmdParts).stream().forEach(p -> logger.info("Command part: [" + p + "]"));

        org.testcontainers.containers.GenericContainer<?> result = new org.testcontainers.containers.GenericContainer<>(imageRef)
            .withCreateContainerCmdModifier(cmd -> cmd.withUser(userStr).withEntrypoint(entrypoint))
            .withCommand(cmdParts)
            .withLogConsumer(frame -> logger.info(frame.getUtf8StringWithoutLineEnding()))
            ;

        for (Bind bind : fileMapper.getBinds()) {
            result = result.withFileSystemBind(bind.getPath(), bind.getVolume().getPath(), ContainerUtils.toBindMode(bind.getAccessMode()));
            logger.info("Adding bind: " + bind);
        }

        return result;
    }

    /** If the op is "/virt/cat path" then return path. */
    public static String extractSimpleCatPath(CmdOp cmdOp) {
        if (cmdOp instanceof CmdOpExec exec) {
            if ("/virt/cat".equals(exec.name())) {
                if (exec.args().size() == 1) {
                    CmdArg a = exec.args().args().get(0);
                    if (a instanceof CmdArgWord w) {
                        if (w.tokens().size() == 1) {
                            Token t = w.tokens().get(0);
                            if (t instanceof TokenPath tp) {
                                 return tp.path();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected ProcessBuilderDocker cloneActual() {
        ProcessBuilderDocker result = new ProcessBuilderDocker();
        applySettings(result);
        return result;
    }

    protected void applySettings(ProcessBuilderDocker target) {
        target.imageRef(imageRef());
        target.entrypoint(entrypoint());
        target.workingDirectory(workingDirectory());
        target.interactive(interactive());
    }
}
