package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.github.dockerjava.api.model.Bind;

import org.aksw.commons.util.docker.ContainerPathResolver;
import org.aksw.commons.util.docker.ContainerUtils;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgWord;
import org.aksw.shellgebra.algebra.cmd.arg.Token;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenPath;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOps;
import org.aksw.shellgebra.algebra.cmd.redirect.CmdRedirect;
import org.aksw.shellgebra.algebra.cmd.transform.CmdString;
import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.exec.graph.JRedirect;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectJava;
import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.aksw.shellgebra.exec.invocation.CompileContext;
import org.aksw.shellgebra.exec.invocation.ExecutableInvocation;
import org.aksw.shellgebra.exec.invocation.Invocation;
import org.aksw.shellgebra.exec.invocation.InvocationCompiler;
import org.aksw.shellgebra.exec.invocation.InvocationCompilerImpl;
import org.aksw.shellgebra.exec.invocation.InvokableProcessBuilderBase;
import org.aksw.shellgebra.exec.invocation.ScriptContent;
import org.aksw.vshell.registry.JvmCommandParser;
import org.aksw.vshell.shim.rdfconvert.Args;
import org.aksw.vshell.shim.rdfconvert.ArgumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process builder that starts a process in a docker container via docker run.
 */
public class ProcessBuilderDocker
    extends InvokableProcessBuilderBase<ProcessBuilderDocker>
{
    private static final Logger logger = LoggerFactory.getLogger(ProcessBuilderDocker.class);

    protected String imageRef;
    protected String entrypoint;
    protected String workingDirectory;
    protected ContainerPathResolver containerPathResolver;
    protected FileMapper fileMapper;
    protected Boolean interactive;

    protected InvocationCompiler compiler;
    protected JvmCommandParser commandParser;

    // protected CmdOp commandOp;

    // XXX In general, we need a CmdOp such that we can auto-map the arguments.
    // We could allow setting a BiFunction<String, Args, CmdOp> parser function for parser registry lookups.
    // protected CmdOpExec cmdOpExec;

    public ProcessBuilderDocker() {
        super();
    }

    @Override
    public boolean supportsAnonPipeRead() {
        return false;
    }

    @Override
    public boolean supportsAnonPipeWrite() {
        return false;
    }

    @Override
    public boolean supportsDirectNamedPipe() {
        return true;
    }

    public static ProcessBuilderDocker of(String ... command) {
        return new ProcessBuilderDocker().command(command);
    }

    public static ProcessBuilderDocker of(List<String> command) {
        return new ProcessBuilderDocker().command(command);
    }

    public String imageRef() {
        return imageRef;
    }

    public ProcessBuilderDocker imageRef(String imageRef) {
        this.imageRef = imageRef;
        return self();
    }

//    public ProcessBuilderDocker commandOp(CmdOp commandOp) {
//        this.commandOp = commandOp;
//        return self();
//    }
//
//    public CmdOp commandOp() {
//        return commandOp;
//    }

//    public String entrypoint() {
//        return entrypoint;
//    }

    public ProcessBuilderDocker interactive(Boolean interactive) {
        this.interactive = interactive;
        return self();
    }

    public Boolean interactive() {
        return interactive;
    }

    public ProcessBuilderDocker commandParser(JvmCommandParser commandParser) {
        this.commandParser = commandParser;
        return self();
    }

    public JvmCommandParser jvmCommandParser() {
        return commandParser;
    }

    /**
     * Set an explicit entrypoint.
     * If none is set, an attempt to infer one from the context will be made upon starting the process.
     *
     * @param entrypoint
     * @return
     */
//    public ProcessBuilderDocker entrypoint(String entrypoint) {
//        this.entrypoint = entrypoint;
//        return self();
//    }

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

    public ProcessBuilderDocker compiler(InvocationCompiler compiler) {
        this.compiler = compiler;
        return self();
    }

    public InvocationCompiler compiler() {
        return compiler;
    }

    @Override
    public Process start(ProcessRunner executor) throws IOException {
        return execInternal(executor);
    }

    // TODO We need to set up a helper cat in-pipe-end > named-pipe
    protected Process catProcess(Path source, Path target) throws IOException {
        CmdOpExec cat = new CmdOpExec(List.of(), "cat", ArgumentList.of(
            CmdArg.ofPathString(source.toString()),
            CmdArg.redirect(CmdRedirect.out(target.toString()))));
        String scriptString = toScriptString(cat);

        ProcessBuilder pb = new ProcessBuilder("bash", "-c", scriptString);
        Process process = pb.start();
        return process;
    }

    // protected Process execToPathInternal(Path outPath, String outContainerPath, PathLifeCycle pathLifeCycle) {
    protected Process execInternal(ProcessRunner executor) throws IOException {
        Objects.requireNonNull(imageRef, "image not set.");

        Invocation inv = invocation();
        if (inv == null) {
            throw new IllegalStateException("No invocation set");
        }

        // Closer closer = Closer.create();
        List<FileWriterTask> inputTasks = new ArrayList<>();

        PathAndProcess inProcess = processInput(executor.inputPipe(), redirectInput());
        PathAndProcess outProcess = processOutput(executor.outputPipe(), redirectOutput());
        PathAndProcess errProcess = processOutput(executor.errorPipe(), redirectError());

        Path hostMountableInputPath = inProcess.path();
        Path hostMountableOutputPath = outProcess.path();
        Path hostMountableErrorPath = errProcess.path();

        FileMapper finalFileMapper = fileMapper.clone();
        /*
        String containerInputPath = finalFileMapper.allocate(hostMountableInputPath.toString(), AccessMode.ro);
        String containerOutputPath = finalFileMapper.allocate(hostMountableOutputPath.toString(), AccessMode.rw);
        String containerErrorPath = finalFileMapper.allocate(hostMountableErrorPath.toString(), AccessMode.rw);
        */

        // Resolver over SysRuntime. Container is started only on-demand.
//        SysRuntimeFactoryDocker sysRuntimeFactory = SysRuntimeFactoryDocker.create();
//
//        CompileContext cxt = CompileContext.of(resolver -> {
//            try (SysRuntime runtime = SysRuntimeCoreLazy.of(() -> sysRuntimeFactory.create(imageRef))) {
//                try {
//                    String resolvedCommand = runtime.which(entrypoint);
//                    return resolvedCommand;
//                } catch (IOException | InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        });
//
//        InvocationCompiler finalCompiler = compiler != null ? compiler : InvocationCompilerImpl.getDefault();
//        ExecutableInvocation exec = finalCompiler.compile(inv, cxt);

        // CmdOp op = CmdOpExec.ofLiteralArgs(super.command().toArray(String[]::new));

        List<String> argv = inv.asArgv().argv();
        List<String> args = argv.subList(1, argv.size());

        CmdOp op;
        boolean actualInteractive;
        Optional<Boolean> baseInteractive = Optional.ofNullable(interactive);
        if (commandParser != null) {
            Args ar = commandParser.parseArgs(args.toArray(String[]::new));
            op = new CmdOpExec(argv.get(0), ar.toArgList());
            actualInteractive = baseInteractive.or(ar::readsStdin).orElse(true);
        } else {
            op = CmdOpExec.ofLiteralArgs(argv);
            actualInteractive = baseInteractive.orElse(true);
        }

        if (actualInteractive) {
            op = CmdOps.appendRedirects(op,
                CmdRedirect.in(hostMountableInputPath.toString()));
        }

        op = CmdOps.appendRedirects(op,
            CmdRedirect.out(hostMountableOutputPath.toString()),
            CmdRedirect.err(hostMountableErrorPath.toString())
        );

        /*
        op = CmdOps.appendRedirects(op,
            CmdRedirect.in(containerInputPath),
            CmdRedirect.out(containerOutputPath),
            CmdRedirect.err(containerErrorPath)
            );
        */
        // If there is a byte source as a file writer then start it.
        org.testcontainers.containers.GenericContainer<?> container;
        try {
            container = setupContainer(op, finalFileMapper);
            Runnable runnable = () -> {
                try {
                    container.start();
                } finally {
                }
            };
            // TODO Probably start container in a thread and then wait for termination
            // so that a final termination callback can be reliably called.
            runnable.run();

            // TODOD In general exec.close() must be called!
            return new ProcessOverDockerContainer(container);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
//
//    @Override
//    public Process start(ProcessRunner executor) throws IOException {
//        Objects.requireNonNull(imageRef, "image not set.");
//
//        // Resolver over SysRuntime. Container is started only on-demand.
//        CompileContext cxt = CompileContext.of(resolver -> {
//            try (SysRuntime runtime = SysRuntimeCoreLazy.of(() -> SysRuntimeFactoryDocker.create().create(imageRef))) {
//                try {
//                    String resolvedCommand = runtime.which(entrypoint);
//                    return resolvedCommand;
//                } catch (IOException | InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        });
//
//        Invocation inv = invocation();
//        if (inv == null) {
//            throw new IllegalStateException("No invocation set");
//        }
//
//        InvocationCompiler finalCompiler = compiler != null ? compiler : InvocationCompilerImpl.getDefault();
//        ExecutableInvocation exec = finalCompiler.compile(inv, cxt);
//        ProcessBuilder pb = new ProcessBuilder();
//        pb.command(exec.argv());
//        ProcessBuilderNative.configure(pb, this, executor);
//        Process p = pb.start();
//        // Cleanup after process exit.
//        p.toHandle().onExit().thenRun(() -> {
//            try {
//                exec.close();
//            } catch (Exception e) {
//                logger.warn("Error during close", e);
//            }
//        });
//        return p;
//    }

    // Perhaps use FileWriter abstraction?
    private record PathAndProcess(Path path, Process process) {}

    private PathAndProcess processInput(Path inputPipePath, JRedirect redirect) throws IOException {
        // Extract effective raw input path
        Path rawInputPath = null;
        if (redirect instanceof JRedirectJava x) {
            Redirect r = x.redirect();
            switch (r.type()) {
            case INHERIT:
                rawInputPath = inputPipePath;
                break;
            case READ:
                rawInputPath = r.file().toPath();
                break;
            default:
                throw new RuntimeException("Unsupported or not implemented yet.");
            }
        }

        // If the raw input path is not docker mountable then we start
        // a helper process that pumps the bytes from the raw input path to a mountable named pipe.
        boolean isRawInputPathMountable = isProbablyDockerBindSource(rawInputPath);
        Path hostMountableInputPath = null;
        Process pumpProcess = null;
        if (isRawInputPathMountable) {
            hostMountableInputPath = rawInputPath;
        } else {
            Path namedPipePath = SysRuntime.newNamedPipe();
            hostMountableInputPath = namedPipePath;
            pumpProcess = catProcess(rawInputPath, hostMountableInputPath);
            // TODO Link pump process life cycle to the returned process.
        }
        return new PathAndProcess(hostMountableInputPath, pumpProcess);
    }

    private PathAndProcess processOutput(Path outputPipePath, JRedirect redirect) throws IOException {
        // Extract effective raw input path
        Path rawPath = null;
        if (redirect instanceof JRedirectJava x) {
            Redirect r = x.redirect();
            switch (r.type()) {
            case INHERIT:
                rawPath = outputPipePath;
                break;
            case WRITE:
                rawPath = r.file().toPath();
                break;
            default:
                throw new RuntimeException("Unsupported or not implemented yet.");
            }
        }

        // If the raw input path is not docker mountable then we start
        // a helper process that pumps the bytes from the raw input path to a mountable named pipe.
        boolean isRawPathMountable = isProbablyDockerBindSource(rawPath);
        Path hostMountablePath = null;
        Process pumpProcess = null;
        if (isRawPathMountable) {
            hostMountablePath = rawPath;
        } else {
            Path namedPipePath = SysRuntime.newNamedPipe();
            hostMountablePath = namedPipePath;
            pumpProcess = catProcess(hostMountablePath, rawPath);
        }
        return new PathAndProcess(hostMountablePath, pumpProcess);
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

    protected org.testcontainers.containers.GenericContainer<?> setupContainer(CmdOp rawCmdOp, FileMapper fileMapper) throws IOException {
        // TODO Consolidate with SysRuntimeCore: Need to get the appropriate bash entry point from some registry.

        CmdOp cmdOp = CmdOpRewriter.rewriteForContainer(rawCmdOp, fileMapper);

        String userStr = getUserString();
        logger.info("Setting up container " + imageRef + " with UID:GID=" + userStr);

        String scriptString = toScriptString(cmdOp);

        // Create an invocation of the script string as an inline bash script.
        Invocation inv = new Invocation.Script(scriptString, ScriptContent.contentTypeBash);

        SysRuntimeFactoryDocker sysRuntimeFactory = SysRuntimeFactoryDocker.create();

        ExecutableInvocation exec;
        // XXX The entrypoint would only be needed to start a container for resolving commands!
        // String actualEntryPoint = entrypoint;
        try (SysRuntime runtime = SysRuntimeCoreLazy.of(() -> sysRuntimeFactory.create(imageRef))) {
//            if (actualEntryPoint == null) {
//                sysRuntimeFactory.create(actualEntryPoint);
//            }
            CompileContext cxt = CompileContext.of(commandName -> {
                try {
                    String resolvedCommand = runtime.which(commandName);
                    return resolvedCommand;
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            InvocationCompiler finalCompiler = compiler != null ? compiler : InvocationCompilerImpl.getDefault();
            exec = finalCompiler.compile(inv, cxt);
        }

        List<String> tmp = exec.argv();
        String actualEntrypoint = tmp.get(0);
        String[] cmdParts = tmp.subList(1, tmp.size()).toArray(String[]::new);

        // String[] cmdParts = exec.argv().toArray(String[]::new);
        List.of(cmdParts).stream().forEach(p -> logger.info("Command part: [" + p + "]"));
        org.testcontainers.containers.GenericContainer<?> result = new org.testcontainers.containers.GenericContainer<>(imageRef)
            .withCreateContainerCmdModifier(cmd -> cmd.withUser(userStr).withEntrypoint(actualEntrypoint))
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

    protected ContainerPathResolver containerPathResolver() {
        return containerPathResolver;
    }

    protected ProcessBuilderDocker containerPathResolver(ContainerPathResolver containerPathResolver) {
        this.containerPathResolver = containerPathResolver;
        return self();
    }

    protected void applySettings(ProcessBuilderDocker target) {
        target.imageRef(imageRef());
        // target.entrypoint(entrypoint());
        target.workingDirectory(workingDirectory());
        target.interactive(interactive());

        target.containerPathResolver(containerPathResolver());
        target.fileMapper(fileMapper());
    }

    private static boolean isAnonymousProcPipe(Path p) throws IOException {
        // Only meaningful on Linux procfs /proc/<pid>/fd/N

        // if (p.startsWith("/proc") && p.toString().contains("/fd/")) return false;
        if (!p.startsWith("/proc")) return false;
        if (!Files.isSymbolicLink(p)) return false;

        Path target = Files.readSymbolicLink(p);
        String s = target.toString();
        // return s.startsWith("pipe:[") || s.startsWith("socket:[") || s.startsWith("anon_inode:[");
        return s.matches("^(pipe|socket|anon_inode):\\[.*\\]$");
    }

    /**
     * Return true if the given path can be bind mounted into a docker container.
     * Specifically, any path starting with /proc is considered to be NOT bind mountable.
     */
    private static boolean isProbablyDockerBindSource(Path p) throws IOException {
        if (!Files.exists(p)) return false;
        return !isAnonymousProcPipe(p);
    }
}

//// String[] entrypoint = new String[]{"bash"};
//String[] cmdParts;
//if (true) { // cmdString.isScriptString()) {
//  cmdParts = new String[] {
//      "-c",
//      // List.of(cmdString.cmd()).stream().collect(Collectors.joining(" "))
//      scriptString
//  };
//} else {
////  CmdStrOps strOps = runtime.getStrOps();
////  cmdParts = cmdString.cmd(); // XXX Must ensure that the command is resolvable!
//}
//
//
//
//InvocationCompiler finalCompiler = compiler != null ? compiler : InvocationCompilerImpl.getDefault();
//ExecutableInvocation exec = finalCompiler.compile(inv, cxt);
