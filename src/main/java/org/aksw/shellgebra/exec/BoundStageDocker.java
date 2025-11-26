package org.aksw.shellgebra.exec;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.aksw.commons.util.docker.ContainerPathResolver;
import org.aksw.jenax.engine.qlever.SystemUtils;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgCmdOp;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgWord;
import org.aksw.shellgebra.algebra.cmd.arg.Token;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenPath;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVisitor;
import org.aksw.shellgebra.algebra.cmd.op.CmdOps;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectFile;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectFile.OpenMode;
import org.aksw.shellgebra.algebra.cmd.transform.CmdString;
import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.algebra.cmd.transformer.CmdOpTransformBase;
import org.aksw.shellgebra.algebra.cmd.transformer.CmdOpTransformer;
import org.aksw.shellgebra.algebra.cmd.transformer.CmdTransformBase;
import org.aksw.vshell.shim.rdfconvert.ArgumentList;
import org.apache.commons.io.IOUtils;
import org.apache.jena.riot.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.async.ResultCallback.Adapter;
import com.github.dockerjava.api.command.AttachContainerCmd;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.google.common.io.ByteSource;

public class BoundStageDocker
    implements BoundStage
{
    private static final Logger logger = LoggerFactory.getLogger(BoundStageDocker.class);

    // A Docker image reference consists of several components that describe where the image is stored and its identity. These components are:
    // https://docs.docker.com/reference/cli/docker/image/tag/ - [HOST[:PORT]/]NAMESPACE/REPOSITORY[:TAG]
    protected String imageRef;

    // protected String imageTag;
    // protected List<String> cmd;
    protected CmdOp cmdOp;
    protected Function<CmdOpVar, Stage> varResolver;
    protected List<Bind> binds;
    protected String workingDirectory;
    protected ContainerPathResolver containerPathResolver;
    protected FileMapper fileMapper;

    protected FileWriterTask inputTask;
    protected BoundStage inputExecBuilder;

    // List<Bind> binds
    public BoundStageDocker(String imageRef, CmdOp cmdOp, FileMapper fileMapper, ContainerPathResolver containerPathResolver, FileWriterTask inputTask, BoundStage inputExecBuilder, Function<CmdOpVar, Stage> varResolver) {
        super();
        this.imageRef = imageRef;
        this.cmdOp = cmdOp;
        this.fileMapper = fileMapper;
        // this.binds = binds;
        this.containerPathResolver = containerPathResolver;
        this.inputTask = inputTask;
        this.inputExecBuilder = inputExecBuilder;

        this.varResolver = varResolver;
        // this.workingDirectory = workingDirectory;
        // this.containerPathResolver = containerPathResolver;
    }

    protected String getUserString() throws IOException {
        int uid = SystemUtils.getUID();
        int gid = SystemUtils.getGID();
        String userStr = uid + ":" + gid;
        return userStr;
    }

//    public static String buildDockerImageName(String imageName, String imageTag) {
//        String tag = getImageTag(imageTag);
//        String image = getImageName(imageName);
//        String result = Stream.of(image, tag)
//            .filter(x -> x != null)
//            .collect(Collectors.joining(":"));
//        return result;
//    }

    protected org.testcontainers.containers.GenericContainer<?> setupContainer(CmdOp cmdOp) throws IOException {
        // TODO Consolidate with SysRuntimeCore: Need to get the appropriate bash entry point from some registry.

        String userStr = getUserString();
        logger.info("Setting up container " + imageRef + " with UID:GID=" + userStr);

        SysRuntime runtime = SysRuntimeImpl.forCurrentOs();

        // TODO Variables need to be resolved - typically involves named pipes.
        // CmdOp op = new CmdOpExec("/usr/bin/bash", new CmdArgLiteral("-c"), new CmdArgCmdOp(cmdOp));

        // The original command becomes an argument to 'shell -c'
        // So compile a string where the original command is used as an argument.
        CmdOp dummy = new CmdOpExec("/dummy", CmdArg.ofCommandSubstitution(cmdOp));
        CmdString cmdString = runtime.compileString(dummy);
        String scriptString = cmdString.cmd()[1];

        String[] entrypoint = new String[]{"bash"};
        String[] cmdParts;
        if (true) { // cmdString.isScriptString()) {
            cmdParts = new String[] {
                "-c",
                // List.of(cmdString.cmd()).stream().collect(Collectors.joining(" "))
                scriptString
            };
        } else {
            CmdStrOps strOps = runtime.getStrOps();
            cmdParts = cmdString.cmd(); // XXX Must ensure that the command is resolvable!
        }
        // String[] cmdParts = cmdString.cmd();

        // String str = cmdString.scriptString();

        // Path outputFolder = config.getOutputFolder();
        // String finalImageName = QleverConstants.buildDockerImageName(imageName, imageTag);
        // Path finalOutputFolder = ContainerPathResolver.resolvePath(containerPathResolver, outputFolder);

        // String[] cmdParts = cmdString.cmd(); // cmd.toArray(new String[0]);
        // String[] cmdStrs = new String[] {"/usr/bin/bash", "-c", str};

        List.of(cmdParts).stream().forEach(p -> logger.info("Command part: [" + p + "]"));

        org.testcontainers.containers.GenericContainer<?> result = new org.testcontainers.containers.GenericContainer<>(imageRef)
            // .withWorkingDirectory(workingDirectory)
            // .withExposedPorts(containerPort)
            // Setting UID does not work with latest image due to
            // error "UID 1000 already exists" ~ 2025-01-31
            // .withEnv("UID", Integer.toString(uid))
            // .withEnv("GID", Integer.toString(gid))
            .withCreateContainerCmdModifier(cmd -> cmd.withUser(userStr).withEntrypoint(entrypoint))
            // .withFileSystemBind(finalOutputFolder.toString(), "/data/", BindMode.READ_WRITE)
            .withCommand(cmdParts)
            .withLogConsumer(frame -> logger.info(frame.getUtf8StringWithoutLineEnding()))
            // .withCommand(new String[]{"ServerMain -h"})
            ;

        for (Bind bind : fileMapper.getBinds()) {
            result = result.withFileSystemBind(bind.getPath(), bind.getVolume().getPath(), toBindMode(bind.getAccessMode()));
            logger.info("Adding bind: " + bind);
        }

        return result;
    }

    @Override
    public ByteSource toByteSource() {
        // TODO Make a snapshot of the builder state here.
        return new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                return execToInputStream();
            }
        };
    }

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

    protected FileWriterTask execToPathInternal(Path outPath, String outContainerPath, PathLifeCycle pathLifeCycle) {
        // Closer closer = Closer.create();

        List<FileWriterTask> inputTasks = new ArrayList<>();

        FileWriterTask itask = inputTask;

        if (inputExecBuilder != null) {
            itask = inputExecBuilder.runToHostPipe();
            String containerPath = fileMapper.allocate(itask.getOutputPath().toAbsolutePath().toString(), AccessMode.ro);
        }

        CmdOp tmpOp = cmdOp;
        if (itask != null) {
//            inputTask.start();
            // Entry<Path, String> inputBind = fileMapper.allocateTempFile("byteSource", "", AccessMode.ro);
            // Note: The input task is mapped immediately on creation.
            String containerPath = fileMapper.getContainerPath(itask.getOutputPath().toString());
            if (containerPath == null) {
                throw new RuntimeException("should not happen");
            }

            // Path hostPath = inputBind.getKey();
            tmpOp = CmdOp.prependRedirect(tmpOp, new RedirectFile(containerPath, OpenMode.READ, 0));
//            closer.register(() -> {
//                try { inputTask.close(); }
//                catch (Exception e) { throw new RuntimeException(e); }
//            });
            // Set up a bind for the input
            // FileWriterTask inputTask = new FileWriterTaskFromByteSource(hostPath, PathLifeCycles.namedPipe(), byteSource);

            inputTasks.add(itask);
        }



        Set<CmdOpVar> vars = CmdOps.accVars(cmdOp);
        // Map<CmdOpVar, Stage> varToStage = new LinkedHashMap<>();
        Map<CmdOpVar, String> varToContainerPath = new LinkedHashMap<>();
        // List<FileWriterTask> subTasks = new ArrayList<>();

        // FIXME We cannot just use stage.fromNull - we need to properly wire up the stages!
        for (CmdOpVar v : vars) {
            Stage stage = varResolver.apply(v);
            // varToStage.put(v, stage);
//            Entry<Path, String> pair = fileMapper.allocateTempFile("", "", AccessMode.ro);
//            Path hostPath = pair.getKey();
//            String containerPath = pair.getValue();
//            FileWriterTask fwt = stage.fromNull().execToFile(hostPath, PathLifeCycles.deleteAfterExec(PathLifeCycles.namedPipe()));
            FileWriterTask fwt = stage.fromNull().runToHostPipe();
            String containerPath = fileMapper.getContainerPath(fwt.getOutputPath().toAbsolutePath().toString());
            Objects.requireNonNull(containerPath);
            varToContainerPath.put(v, containerPath);
            inputTasks.add(fwt);
            // substitutions.put(v, CmdOp.);
            // String containerPath = fileMapper.allocate(itask.getOutputPath().toAbsolutePath().toString(), AccessMode.ro);

            // stage.
            // stage.fromNull().runToHostPipe()
        }

        tmpOp = CmdOpTransformer.transform(tmpOp, new CmdTransformBase() {
            @Override
            public CmdOp transform(CmdOpVar op) {
                String containerPath = varToContainerPath.get(op);
                String catCommand = "cat";

                return new CmdOpExec(catCommand, ArgumentList.of(CmdArg.ofPathString(containerPath)));
            }

            @Override
            public CmdArg transform(CmdArgCmdOp arg, CmdOp subOp) {
                String path = extractSimpleCatPath(subOp);
                CmdArg r = path != null
                    ? CmdArg.ofPathString(path)
                    : CmdTransformBase.super.transform(arg, subOp);
                return r;
            }
        });

        // SysRuntimeImpl.forCurrentOs().createNamedPipe(outPipePath);
        CmdOp effectiveOp = CmdOp.appendRedirect(tmpOp, RedirectFile.fileToStdOut(outContainerPath, OpenMode.WRITE_TRUNCATE));

        // effectiveOp = tmpOp;

//        CmdString cmdString = SysRuntime.toString(effectiveOp);
//        String[] parts = cmdString.cmd();

        // If there is a byte source as a file writer then start it.
        org.testcontainers.containers.GenericContainer<?> container;
        try {
            container = setupContainer(effectiveOp);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        FileWriterTask task = new FileWriterTaskFromContainer(container, outPath, pathLifeCycle, inputTasks);
        return task;
    }

    @Override
    public FileWriterTask execToRegularFile(Path hostPath) {
        return execToFile(hostPath, PathLifeCycles.none());
    }

    @Override
    public FileWriterTask execToFile(Path hostPath, PathLifeCycle pathLifeCycle) {
        String hostPathStr = hostPath.toAbsolutePath().toString();
        fileMapper.allocate(hostPathStr, AccessMode.rw);
        return execToPathInternal(hostPath, hostPathStr, pathLifeCycle);
    }

    public InputStream execToInputStream() throws IOException {
        PathLifeCycle pathLifeCycle = PathLifeCycles.deleteAfterExec(PathLifeCycles.namedPipe());

        // Create the output file - container writes, host reads.
        Entry<Path, String> map = fileMapper.allocateTempFile("", "", AccessMode.rw);
        Path outPipePath = map.getKey();
        String outContainerPath = map.getValue();

        FileWriterTask fileWriterTask = execToPathInternal(outPipePath, outContainerPath, pathLifeCycle);
        fileWriterTask.start();

        InputStream in = Files.newInputStream(outPipePath); //, StandardOpenOption.READ);
        FilterInputStream result = new FilterInputStream(in) {
            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    try {
                        fileWriterTask.close();
                    } catch (Exception e) {
                        logger.warn("Failure during close", e);
                    }
                }
            }
        };

        return result;
    }

    private static BindMode toBindMode(AccessMode am) {
        return am == AccessMode.ro ? BindMode.READ_ONLY : BindMode.READ_WRITE;
    }

    // For an input stream, pipe it to a named pipe and supply it via bash
    protected void runContainerWithInputStream(ByteSource byteSource, Lang lang) throws InterruptedException, IOException {

        logger.info("Attempting to launch container with a JVM-based input stream.");
        org.testcontainers.containers.GenericContainer<?> container = setupContainer(null)
            .withCreateContainerCmdModifier(cmd -> cmd
                // .withTty(true)         // Required to keep input open
                .withTty(false)
                // .withStdInOnce(true)
                .withStdinOpen(true)
                .withAttachStdin(true) // Allow attaching input stream
                // .withAttachStdout(true)
                // .withAttachStderr(true)
            );

            container.start();
//            container.followOutput(outputFrame -> {
//                String msg = outputFrame.getUtf8String();
//                logger.info(msg);
//            });

            System.out.println("Waiting");
            Thread.sleep(2000);
            System.out.println("Attaching data");

        // Get input stream (e.g., file or command output)
        try (InputStream in = byteSource.openStream()) {
            String str = IOUtils.toString(in, StandardCharsets.UTF_8);
            System.out.println(str);
            InputStream is = new ByteArrayInputStream(str.getBytes());

            // BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharset.UTF_8));
            // br.lines().forEach(System.out::println);

            // Attach input stream to the container
            // Adapter<Frame> xxx =
                AttachContainerCmd tmp = container.getDockerClient()
                .attachContainerCmd(container.getContainerId())
                .withStdIn(is)
                // .withStdErr(true)
                // .withStdOut(true)
                // .withFollowStream(true)
                ;
                // .withLogs(true);


               Adapter<Frame> callback = new ResultCallback.Adapter<Frame>() {
                    @Override
                    public void onNext(Frame frame) {
                        String msg = new String(frame.getPayload(), StandardCharsets.UTF_8);
                        logger.info(msg);
                        super.onNext(frame);
                    }
               };
               System.out.println("Waiting");
               Thread.sleep(5000);
               System.out.println("Awaiting completion");
               tmp.exec(callback).awaitCompletion();

               // tmp.exec(new AttachContainerResultCallback()).awaitCompletion();

            // x.exec(new AttachContainerResultCallback()).awaitCompletion();

            // ResultCallbackTemplate<?, Frame> foo = x.start();
            System.out.println("Done");

            // x.getStdin()
                //.exec(new AttachContainerResultCallback());
                // .awaitCompletion();f
            // container.waitingFor(WaitStrategy)
        }

        container.getDockerClient()
            .waitContainerCmd(container.getContainerId())
            .exec(new WaitContainerResultCallback())
            .awaitCompletion();

        container.stop();
    }

    @Override
    public FileWriterTask runToHostPipe() {
        PathLifeCycle pathLifeCycle = PathLifeCycles.deleteAfterExec(PathLifeCycles.namedPipe());
        Path tempFile = FileMapper.allocateTempPath("", "");
        return execToFile(tempFile, pathLifeCycle);
    }
}
