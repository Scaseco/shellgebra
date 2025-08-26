package org.aksw.shellgebra.exec;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.aksw.commons.util.docker.ContainerPathResolver;
import org.aksw.jenax.engine.qlever.SystemUtils;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgCmdOp;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgLiteral;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectFile;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectFile.OpenMode;
import org.aksw.shellgebra.algebra.cmd.transform.CmdString;
import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.exec.FileWriterTaskBase.PathLifeCycle;
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

public class ExecBuilderDocker {
    private static final Logger logger = LoggerFactory.getLogger(ExecBuilderDocker.class);

    // A Docker image reference consists of several components that describe where the image is stored and its identity. These components are:
    // https://docs.docker.com/reference/cli/docker/image/tag/ - [HOST[:PORT]/]NAMESPACE/REPOSITORY[:TAG]
    protected String imageRef;

    // protected String imageTag;
    // protected List<String> cmd;
    protected CmdOp cmdOp;
    protected List<Bind> binds;
    protected String workingDirectory;
    protected ContainerPathResolver containerPathResolver;
    protected FileMapper fileMapper;
    protected FileWriterTask inputTask;

    // List<Bind> binds
    public ExecBuilderDocker(String imageRef, CmdOp cmdOp, FileMapper fileMapper, ContainerPathResolver containerPathResolver) {
        super();
        this.imageRef = imageRef;
        this.cmdOp = cmdOp;
        this.fileMapper = fileMapper;
        // this.binds = binds;
        this.containerPathResolver = containerPathResolver;
        // this.workingDirectory = workingDirectory;
        // this.containerPathResolver = containerPathResolver;
    }

    public static ExecBuilderDocker of(String imageRef, CmdOp cmdOp, FileMapper fileMapper) { // List<Bind> binds) {
        ContainerPathResolver containerPathResolver = ContainerPathResolver.create();
        return new ExecBuilderDocker(imageRef, cmdOp, fileMapper, containerPathResolver);
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

    protected org.testcontainers.containers.GenericContainer<?> setupContainer(CmdOp cmdOp) throws NumberFormatException, IOException, InterruptedException {
        String userStr = getUserString();
        logger.info("Setting up container with UID:GID=" + userStr);

        SysRuntime runtime = SysRuntimeImpl.forCurrentOs();
        CmdOp op = new CmdOpExec("/usr/bin/bash", new CmdArgLiteral("-c"), new CmdArgCmdOp(cmdOp));
        CmdString cmdString = runtime.compileString(cmdOp);
        String[] cmdParts = new String[] {
            "/usr/bin/bash", "-c",
            List.of(cmdString.cmd()).stream().collect(Collectors.joining(" "))
        };


        // String str = cmdString.scriptString();

        // Path outputFolder = config.getOutputFolder();
        // String finalImageName = QleverConstants.buildDockerImageName(imageName, imageTag);
        // Path finalOutputFolder = ContainerPathResolver.resolvePath(containerPathResolver, outputFolder);

        // String[] cmdParts = cmdString.cmd(); // cmd.toArray(new String[0]);
        // String[] cmdStrs = new String[] {"/usr/bin/bash", "-c", str};

        List.of(cmdParts).stream().forEach(p -> System.out.println("[" + p + "]"));

        org.testcontainers.containers.GenericContainer<?> result = new org.testcontainers.containers.GenericContainer<>(imageRef)
            // .withWorkingDirectory(workingDirectory)
            // .withExposedPorts(containerPort)
            // Setting UID does not work with latest image due to
            // error "UID 1000 already exists" ~ 2025-01-31
            // .withEnv("UID", Integer.toString(uid))
            // .withEnv("GID", Integer.toString(gid))
            .withCreateContainerCmdModifier(cmd -> cmd.withUser(userStr))
            // .withFileSystemBind(finalOutputFolder.toString(), "/data/", BindMode.READ_WRITE)
            .withCommand(cmdParts)
            .withLogConsumer(frame -> logger.info(frame.getUtf8StringWithoutLineEnding()))
            // .withCommand(new String[]{"ServerMain -h"})
            ;

        for (Bind bind : fileMapper.getBinds()) {
            result = result.withFileSystemBind(bind.getPath(), bind.getVolume().getPath(), toBindMode(bind.getAccessMode()));
        }

        return result;
    }

    public ByteSource asByteSource() {
        // TODO Make a snapshot of the builder state here.
        return new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                try {
                    return execToInputStream();
                } catch (NumberFormatException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    protected FileWriterTask execToPathInternal(Path outPath, String outContainerPath, PathLifeCycle pathLifeCycle) throws NumberFormatException, IOException, InterruptedException {
        // Closer closer = Closer.create();

        List<FileWriterTask> inputTasks = new ArrayList<>();

        CmdOp tmpOp = cmdOp;
        if (inputTask != null) {
//            inputTask.start();
            Entry<Path, String> inputBind = fileMapper.allocateTempFile("byteSource", "", AccessMode.ro);
            // Path hostPath = inputBind.getKey();
            tmpOp = CmdOp.prependRedirect(tmpOp, new RedirectFile(inputBind.getValue(), OpenMode.READ, 0));
//            closer.register(() -> {
//                try { inputTask.close(); }
//                catch (Exception e) { throw new RuntimeException(e); }
//            });
            // Set up a bind for the input
            // FileWriterTask inputTask = new FileWriterTaskFromByteSource(hostPath, PathLifeCycles.namedPipe(), byteSource);

            inputTasks.add(inputTask);
        }

        // SysRuntimeImpl.forCurrentOs().createNamedPipe(outPipePath);
        CmdOp effectiveOp = CmdOp.appendRedirect(tmpOp, RedirectFile.fileToStdOut(outContainerPath, OpenMode.WRITE_TRUNCATE));

        // effectiveOp = tmpOp;

//        CmdString cmdString = SysRuntime.toString(effectiveOp);
//        String[] parts = cmdString.cmd();

        // If there is a byte source as a file writer then start it.
        org.testcontainers.containers.GenericContainer<?> container = setupContainer(effectiveOp);

        FileWriterTask task = new FileWriterTaskFromContainer(container, outPath, pathLifeCycle, inputTasks);

        return task;
    }

    public FileWriterTask execToRegularFile(Path hostPath) throws NumberFormatException, IOException, InterruptedException {
        return execToFile(hostPath, PathLifeCycles.none());
    }

    public FileWriterTask execToFile(Path hostPath, PathLifeCycle pathLifeCycle) throws NumberFormatException, IOException, InterruptedException {
        String hostPathStr = hostPath.toAbsolutePath().toString();
        fileMapper.allocate(hostPathStr, AccessMode.rw);
        return execToPathInternal(hostPath, hostPathStr, pathLifeCycle);
    }

    public InputStream execToInputStream() throws NumberFormatException, IOException, InterruptedException {
        PathLifeCycle pathLifeCycle = PathLifeCycles.deleteAfterExec(PathLifeCycles.namedPipe());

        Entry<Path, String> map = fileMapper.allocateTempFile("", "", AccessMode.rw);
        Path outPipePath = map.getKey();
        String outContainerPath = map.getValue();

        FileWriterTask fileWriterTask =  execToPathInternal(outPipePath, outContainerPath, pathLifeCycle);
        fileWriterTask.start();

        InputStream in = Files.newInputStream(outPipePath, StandardOpenOption.READ);
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

    // Allocates a temp pipe name
    public FileWriterTask runToHostPipe() {
        return null;
    }


    // TODO Supply input stream - mount via named pipe
    public void setByteSource(ByteSource byteSource) {
        // Allocate a tmp path
        // String allocate(String hostPath, AccessMode accessMode) {

        // TODO Must create the file writer on demand!
        Entry<Path, String> map = fileMapper.allocateTempFile("byteSource", "", AccessMode.ro);

        Path hostPath = map.getKey();

        // Set up a bind for the input
        FileWriterTask inputTask = new FileWriterTaskFromByteSource(hostPath, PathLifeCycles.namedPipe(), byteSource);
        setFileWriter(inputTask);
    }

    public void setFileWriter(FileWriterTask inputTask) {
        this.inputTask = inputTask;
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
}
