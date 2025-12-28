package org.aksw.shellgebra.exec;

//public class CommandBuilder {
//    private CmdOp cmdOp;
//    private List<FileWriterTask> fileWriterTasks;
//
//    private ByteSource inputByteSource;
//    private Path inputHostPath;
//
//    public CommandBuilder(CmdOp cmdOp, List<FileWriterTask> fileWriterTasks) {
//        super();
//        this.cmdOp = cmdOp;
//        this.fileWriterTasks = fileWriterTasks;
//    }
//
//    // TODO Supply input stream - mount via named pipe
//    public void setByteSource(ByteSource byteSource) {
//        // Allocate a tmp path
//        // String allocate(String hostPath, AccessMode accessMode) {
//
//        // TODO Must create the file writer on demand!
//        Entry<Path, String> map = fileMapper.allocateTempFile("byteSource", "", AccessMode.ro);
//
//        Path hostPath = map.getKey();
//
//        // Set up a bind for the input
//        FileWriterTask inputTask = new FileWriterTaskFromByteSource(hostPath, PathLifeCycles.namedPipe(), byteSource);
//        setFileWriter(inputTask);
//    }
//
//    public void setFileWriter(FileWriterTask inputTask) {
//        this.inputTask = inputTask;
//    }
//
//    public void test() {
//
//    }
//}
