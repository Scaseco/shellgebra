package org.aksw.shellgebra.exec;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArgCmdOp;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgLiteral;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.transform.CmdString;
import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.exec.FileWriterTaskBase.PathLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteSource;

public class BoundStageHost
    implements BoundStage
{
    private static final Logger logger = LoggerFactory.getLogger(StageHost.class);

    protected CmdOp cmdOp;

    protected BoundStage inputExecBuilder = null;
    protected FileWriterTask inputTask = null;
    protected ByteSource inputSource = null;
    // protected String workingDirectory;

    protected List<FileWriterTask> dependentTasks;

    public BoundStageHost(CmdOp cmdOp, ByteSource inputSource) {
        super();
        this.cmdOp = cmdOp;
        this.inputSource = inputSource;
    }

    public BoundStageHost(CmdOp cmdOp, FileWriterTask inputTask) {
        super();
        this.cmdOp = cmdOp;
        this.inputTask = inputTask;
    }

    public BoundStageHost(CmdOp cmdOp, BoundStage inputExecBuilder) {
        super();
        this.cmdOp = cmdOp;
        this.inputExecBuilder = inputExecBuilder;
    }

    public BoundStageHost(CmdOp cmdOp) {
        super();
        this.cmdOp = cmdOp;
    }

    protected List<ProcessBuilder> setupProcessBuilders(CmdOp cmdOp) {
        SysRuntime runtime = SysRuntimeImpl.forCurrentOs();
        CmdOp op = new CmdOpExec("/usr/bin/bash", new CmdArgLiteral("-c"), new CmdArgCmdOp(cmdOp));
        CmdString cmdString = runtime.compileString(cmdOp);
        String[] cmdParts = new String[] {
            "/usr/bin/bash", "-c",
            List.of(cmdString.cmd()).stream().collect(Collectors.joining(" "))
        };

        List.of(cmdParts).stream().forEach(p -> System.out.println("[" + p + "]"));

        ProcessBuilder result = new ProcessBuilder(cmdParts);
        return List.of(result);
    }

    protected FileWriterTask execToPathInternal(Path outPath, PathLifeCycle pathLifeCycle) {
        List<FileWriterTask> inputTasks = new ArrayList<>();
        List<ProcessBuilder> processBuilders = setupProcessBuilders(cmdOp);
//        List<Process> processes = ProcessBuilder.startPipeline(processBuilders);
//
//        // Configure input from a file.
//        CmdOp tmpOp = cmdOp;
//        if (inputTask != null) {
//            Path file = inputTask.getOutputPath();
//            tmpOp = CmdOp.prependRedirect(tmpOp, new RedirectFile(file.toAbsolutePath().toString(), OpenMode.READ, 0));
//            inputTasks.add(inputTask);
//        }
//
//        String outPathStr = outPath.toAbsolutePath().toString();
//        CmdOp effectiveOp = CmdOp.appendRedirect(tmpOp, RedirectFile.fileToStdOut(outPathStr, OpenMode.WRITE_TRUNCATE));
//
//        Process firstProcess = processes.get(0);
//        Process lastProcess = processes.get(processes.size() - 1);

//        // Configure input from a byte source.
//        if (inputSource != null) {
//            try (OutputStream out = firstProcess.getOutputStream()) {
//                try (InputStream in = inputSource.openStream()) {
//                    in.transferTo(out);
//                }
//            }
//        }

        FileWriterTask task = new FileWriterTaskFromProcessBuilder(outPath, pathLifeCycle, processBuilders, inputTasks, inputSource, null);
        return task;
    }

    @Override
    public FileWriterTask execToRegularFile(Path hostPath) {
        return execToFile(hostPath, PathLifeCycles.none());
    }

    @Override
    public FileWriterTask execToFile(Path hostPath, PathLifeCycle pathLifeCycle) {
        return execToPathInternal(hostPath, pathLifeCycle);
    }

    @Override
    public ByteSource toByteSource() {
        List<ProcessBuilder> processBuilders;
        try {
            processBuilders = setupProcessBuilders(cmdOp);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<FileWriterTask> subTasks = dependentTasks == null ? List.of() : List.copyOf(dependentTasks);
        ByteSource result = new ByteSourceOverPipeline(processBuilders, subTasks, inputSource);
        return result;
    }

    // Allocates a temp pipe name
    @Override
    public FileWriterTask runToHostPipe() {
        PathLifeCycle pathLifeCycle = PathLifeCycles.deleteAfterExec(PathLifeCycles.namedPipe());
        Path tempFile = FileMapper.allocateTempPath("", "");
        return execToPathInternal(tempFile, pathLifeCycle);
    }

    // TODO Supply input stream - mount via named pipe

//    @Override
//    public X setInput(ByteSource byteSource) {
//        this.inputSource = byteSource;
//        return self();
//    }
//
//    @Override
//    public X setInput(FileWriterTask inputTask) {
//        this.inputTask = inputTask;
//        return self();
//    }
//
//    @Override
//    public X setInput(ExecBuilder execBuilder) {
//        this.inputExecBuilder = execBuilder;
//        return self();
//    }
//
//    public void setInputPipeLine(List<ProcessBuilder> inputPipeline) {
//        throw new UnsupportedOperationException("not yet implemented");
//    }
//
//    @SuppressWarnings("unchecked")
//    public X self() {
//        return (X)this;
//    }
}
