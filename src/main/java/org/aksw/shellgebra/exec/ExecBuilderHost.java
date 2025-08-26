package org.aksw.shellgebra.exec;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArgCmdOp;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgLiteral;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectFile;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectFile.OpenMode;
import org.aksw.shellgebra.algebra.cmd.transform.CmdString;
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
import com.github.dockerjava.api.model.Frame;
import com.google.common.io.ByteSource;
import com.nimbusds.jose.util.StandardCharset;


public class ExecBuilderHost {
    private static final Logger logger = LoggerFactory.getLogger(ExecBuilderHost.class);

    protected CmdOp cmdOp;

    // Input task can be:
    // - java input stream
    // - another command / process builder
    // - a local file
    protected FileWriterTask inputTask;
    protected ByteSource inputSource;


    // protected String workingDirectory;

    public ExecBuilderHost(CmdOp cmdOp) {
        super();
        // ProcessBuilder.startPipeline(null);
        ProcessBuilder x;
        this.cmdOp = cmdOp;
    }

    public static ExecBuilderHost of(CmdOp cmdOp) {
        return new ExecBuilderHost(cmdOp);
    }

    protected ProcessBuilder setupProcessBuilder(CmdOp cmdOp) throws NumberFormatException, IOException, InterruptedException {
        SysRuntime runtime = SysRuntimeImpl.forCurrentOs();
        CmdOp op = new CmdOpExec("/usr/bin/bash", new CmdArgLiteral("-c"), new CmdArgCmdOp(cmdOp));
        CmdString cmdString = runtime.compileString(cmdOp);
        String[] cmdParts = new String[] {
            "/usr/bin/bash", "-c",
            List.of(cmdString.cmd()).stream().collect(Collectors.joining(" "))
        };

        List.of(cmdParts).stream().forEach(p -> System.out.println("[" + p + "]"));

        ProcessBuilder result = new ProcessBuilder(cmdParts);
        return result;
    }

    public ByteSource asByteSource() {
        return null;
    }

    protected List<Process> execToProcesses() {

    }

    protected FileWriterTask execToPathInternal(Path outPath, PathLifeCycle pathLifeCycle) throws NumberFormatException, IOException, InterruptedException {
        List<FileWriterTask> inputTasks = new ArrayList<>();

        List<ProcessBuilder> processBuilders = List.of();
        List<Process> processes = ProcessBuilder.startPipeline(processBuilders);

        Process firstProcess = processes.get(0);
        Process lastProcess = processes.get(processes.size() - 1);

        if (inputSource != null) {
            try (OutputStream out = firstProcess.getOutputStream()) {
                try (InputStream in = inputSource.openStream()) {
                    in.transferTo(out);
                }
            }
        }

        CmdOp tmpOp = cmdOp;
        if (inputTask != null) {
            Path file = inputTask.getOutputPath();
            tmpOp = CmdOp.prependRedirect(tmpOp, new RedirectFile(file.toAbsolutePath().toString(), OpenMode.READ, 0));
            inputTasks.add(inputTask);
        }

        String outPathStr = outPath.toAbsolutePath().toString();
        CmdOp effectiveOp = CmdOp.appendRedirect(tmpOp, RedirectFile.fileToStdOut(outPathStr, OpenMode.WRITE_TRUNCATE));

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
        org.testcontainers.containers.GenericContainer<?> container = setupProcessBuilder(null)
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
                        String msg = new String(frame.getPayload(), StandardCharset.UTF_8);
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
