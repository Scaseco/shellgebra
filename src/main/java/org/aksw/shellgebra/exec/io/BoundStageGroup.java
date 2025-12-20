package org.aksw.shellgebra.exec.io;

//public class BoundStageGroup
//    implements BoundStage
//{
//    protected List<BoundStage> boundStages;
//
//    @Override
//    public ByteSource toByteSource() {
//        List<ByteSource> byteSources = new ArrayList<>(boundStages.size());
//        for (BoundStage boundStage : boundStages) {
//            ByteSource byteSource = boundStage.toByteSource();
//            byteSources.add(byteSource);
//        }
//        return ByteSource.concat(byteSources);
//    }
//
//    protected List<ProcessBuilder> setupProcessBuilders(CmdOp cmdOp) {
//        SysRuntime runtime = SysRuntimeImpl.forCurrentOs();
//        CmdOp op = new CmdOpExec("/usr/bin/bash", new CmdArgLiteral("-c"), new CmdArgCmdOp(cmdOp));
//        CmdString cmdString = runtime.compileString(cmdOp);
//        String[] cmdParts = new String[] {
//            "/usr/bin/bash", "-c",
//            List.of(cmdString.cmd()).stream().collect(Collectors.joining(" "))
//        };
//
//        List.of(cmdParts).stream().forEach(p -> System.out.println("[" + p + "]"));
//
//        ProcessBuilder result = new ProcessBuilder(cmdParts);
//        return List.of(result);
//    }
//
//    protected FileWriterTask execToPathInternal(Path outPath, PathLifeCycle pathLifeCycle) {
//        List<FileWriterTask> inputTasks = new ArrayList<>();
//        List<ProcessBuilder> processBuilders = setupProcessBuilders(cmdOp);
////        List<Process> processes = ProcessBuilder.startPipeline(processBuilders);
////
////        // Configure input from a file.
////        CmdOp tmpOp = cmdOp;
////        if (inputTask != null) {
////            Path file = inputTask.getOutputPath();
////            tmpOp = CmdOp.prependRedirect(tmpOp, new RedirectFile(file.toAbsolutePath().toString(), OpenMode.READ, 0));
////            inputTasks.add(inputTask);
////        }
////
////        String outPathStr = outPath.toAbsolutePath().toString();
////        CmdOp effectiveOp = CmdOp.appendRedirect(tmpOp, RedirectFile.fileToStdOut(outPathStr, OpenMode.WRITE_TRUNCATE));
////
////        Process firstProcess = processes.get(0);
////        Process lastProcess = processes.get(processes.size() - 1);
//
////        // Configure input from a byte source.
////        if (inputSource != null) {
////            try (OutputStream out = firstProcess.getOutputStream()) {
////                try (InputStream in = inputSource.openStream()) {
////                    in.transferTo(out);
////                }
////            }
////        }
//
//        FileWriterTask task = new FileWriterTaskFromProcessBuilder(outPath, pathLifeCycle, processBuilders, inputTasks, inputSource, null);
//        return task;
//    }
//
//    @Override
//    public FileWriterTask execToRegularFile(Path hostPath) {
//        return execToFile(hostPath, PathLifeCycles.none());
//    }
//
//    @Override
//    public FileWriterTask execToFile(Path hostPath, PathLifeCycle pathLifeCycle) {
//        return execToPathInternal(hostPath, pathLifeCycle);
//    }
//
//}
