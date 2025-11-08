package org.aksw.shellgebra.exec;

import java.util.List;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;

import com.google.common.io.ByteSource;

public class StageHost
    implements Stage
{
    protected CmdOp cmdOp;

    // Input task can be:
    // - java input stream
    // - another command -> another pipeline / list of process builder
    // - a local file
    protected List<FileWriterTask> dependentTasks;

    public StageHost(CmdOp cmdOp) {
        super();
        this.cmdOp = cmdOp;
    }

    @Override
    public BoundStage from(ByteSource byteSource) {
        return new BoundStageHost(cmdOp, byteSource);
    }

    @Override
    public BoundStage from(FileWriterTask inputTask) {
        return new BoundStageHost(cmdOp, inputTask);
    }

    @Override
    public BoundStage from(BoundStage execBuilder) {
        return new BoundStageHost(cmdOp, execBuilder);
    }

    @Override
    public BoundStage fromNull() {
        return new BoundStageHost(cmdOp);
    }
}
