package org.aksw.shellgebra.model.pipeline;

// So the annoying part is that we don't want to execute the whole pipeline, but
// just build a spec with the final command line all the named pipes set up appropriately.

//interface ToolCaps {
//    boolean supportsStdIn();
//    boolean supportsStdOut();
//
//    CliArgLineBuilder newCliArgLineBuilder();
//}
//
//interface CliArgLineBuilder {
//    CliArgLineBuilder setInput(String filename);
//    CliArgLineBuilder setOutput(String filename);
//    List<String> build(); // Return the arg lin.
//}
//
//
//
//public class PipelineFromDocker {
//    private CmdOp cmdOp;
//    private String dockerImage;
//
//    public PipelineFromDocker(CmdOp cmdOp, String dockerImage) {
//        super();
//        this.cmdOp = cmdOp;
//        this.dockerImage = dockerImage;
//    }
//
//    public InputBuilder newBuilder() {
//
//    }
//}
//
//class OutputBuilderViaDocker
//    implements OutputBuilder
//{
//
//    @Override
//    public InputStream toInputStream() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public Future<Integer> toPath(Path outFile) {
//        // 1. Mount the output hostFile into the container
//        CmdOpRedirect redirect = new CmdOpRedirect(fileName, cmdOp);
//
//        // 2. In the container run cmd > containerFile
//        return null;
//    }
//
//    @Override
//    public ExecSpec toExecSpec() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//}
//
//
//class InputBuilderFromDocker
//    implements InputBuilder {
//
//    @Override
//    public OutputBuilder from(InputStream tmpIn) {
//        Path baseDir = Path.of(System.getProperty("java.io.tmpdir"));
//        Path tmpPath = Files.createTempFile(baseDir, "fifo", "");
//
//        CmdOp cmdOp = null;
//
//        // Turn the command into
//        // cat input | originalCommand
//        CmdOp catOp = CmdOpExec.ofLiterals("/usr/bin/cat", "containerFile");
//        CmdOp pipeOp = new CmdOpPipe(catOp, cmdOp);
//
//        // TODO Actually must locate the cat tool first!
//
//        // Path namedPipe = SysRuntimeImpl.forCurrentOs().createNamedPipe(null);
//
//        new FileWriterTaskFromProcess()
//    }
//
//    @Override
//    public OutputBuilder from(Path path) {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public OutputBuilder from(ProcessBuilder processBuilder) {
//
//        // Create a FileWriter that writes to a named pipe.
//
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//}


//org.testcontainers.containers.GenericContainer<?> container = setupContainer(finalIndexName, optsStr)
//.withCreateContainerCmdModifier(cmd -> cmd
//    // .withTty(true)         // Required to keep input open
//    .withTty(false)
//    // .withStdInOnce(true)
//    .withStdinOpen(true)
//    .withAttachStdin(true) // Allow attaching input stream
//    // .withAttachStdout(true)
//    // .withAttachStderr(true)
//);
//
//container.start();
////    container.followOutput(outputFrame -> {
////        String msg = outputFrame.getUtf8String();
////        logger.info(msg);
////    });
//
//System.out.println("Waiting");
//Thread.sleep(2000);
//System.out.println("Attaching data");
//
//// Get input stream (e.g., file or command output)
//try (InputStream in = tmpIn) {
//String str = IOUtils.toString(in, StandardCharsets.UTF_8);
//System.out.println(str);
//InputStream is = new ByteArrayInputStream(str.getBytes());
//
//// BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharset.UTF_8));
//// br.lines().forEach(System.out::println);
//
//// Attach input stream to the container
//// Adapter<Frame> xxx =
//    AttachContainerCmd tmp = container.getDockerClient()
//    .attachContainerCmd(container.getContainerId())
//    .withStdIn(is)
//    // .withStdErr(true)
//    // .withStdOut(true)
//    // .withFollowStream(true)
//    ;
//    // .withLogs(true);
//
//
//   Adapter<Frame> callback = new ResultCallback.Adapter<Frame>() {
//        @Override
//        public void onNext(Frame frame) {
//            String msg = new String(frame.getPayload(), StandardCharset.UTF_8);
//            logger.info(msg);
//            super.onNext(frame);
//        }
//   };
//   System.out.println("Waiting");
//   Thread.sleep(5000);
//   System.out.println("Awaiting completion");
//   tmp.exec(callback).awaitCompletion();
//
//   // tmp.exec(new AttachContainerResultCallback()).awaitCompletion();
//
//// x.exec(new AttachContainerResultCallback()).awaitCompletion();
//
//// ResultCallbackTemplate<?, Frame> foo = x.start();
//System.out.println("Done");
//
//// x.getStdin()
//    //.exec(new AttachContainerResultCallback());
//    // .awaitCompletion();f
//// container.waitingFor(WaitStrategy)
//}
//
//container.getDockerClient()
//.waitContainerCmd(container.getContainerId())
//.exec(new WaitContainerResultCallback())
//.awaitCompletion();
//
//container.stop();


