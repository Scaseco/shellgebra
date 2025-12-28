package org.aksw.shellgebra.exec;

/** Writer task based on a system process created from a system call. */
//public class FileWriterTaskFromBoundStages extends FileWriterTaskViaExecutor {
//    private List<BoundStage> boundStages;
//    // private List<Process> processes;
//
//    private List<FileWriterTask> inputTasks;
//    private ByteSource inputSource;
//    private OutputStreamTransform outputTransform;
//
//    public FileWriterTaskFromBoundStages(Path outputPath, PathLifeCycle pathLifeCycle, List<BoundStage> boundStages, List<FileWriterTask> inputTasks, ByteSource inputSource, OutputStreamTransform outputTransform) {
//        super(outputPath, pathLifeCycle);
//        this.boundStages = boundStages;
//        this.inputTasks = inputTasks;
//        this.inputSource = inputSource;
//        this.outputTransform = Optional.ofNullable(outputTransform).orElse(o -> o);
//    }
//
//    protected final void beforeExec() throws IOException {
//        pathLifeCycle.beforeExec(outputPath);
//    }
//
//    @Override
//    protected void prepareWriteFile() throws IOException {
//    }
//
//    @Override
//    public void runWriteFile() throws ExecuteException, IOException {
//        for (FileWriterTask inputTask : inputTasks) {
//            inputTask.start();
//        }
//
//
//        for (BoundStage boundStage : boundStages) {
//            FileWriterTask task = boundStage.execToRegularFile(outputPath);
//
//            if (inputSource != null) {
//                Process firstProcess = processes.get(0);
//                try (OutputStream out = outputTransform.apply(firstProcess.getOutputStream())) {
//                    try (InputStream in = inputSource.openStream()) {
//                        in.transferTo(out);
//                    }
//                    out.flush();
//                }
//            }
//        }
//    }
//
//    @Override
//    public void abortActual() {
//        for (FileWriterTask inputTask : inputTasks) {
//            inputTask.abort();
//        }
//
//        for (Process process : processes) {
//            process.destroy();
//        }
//    }
//
//    @Override
//    protected void onCompletion() throws IOException {
//        // nothing to do
//    }
//
//    @Override
//    public String toString() {
//        return super.toString(); // + " " + cmdLine;
//    }
//}
