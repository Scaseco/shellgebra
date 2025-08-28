package org.aksw.shellgebra.exec;

import java.util.List;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;

import com.google.common.io.ByteSource;

public class HostStage
    implements Stage
{
    protected CmdOp cmdOp;

    // Input task can be:
    // - java input stream
    // - another command -> another pipeline / list of process builder
    // - a local file
    protected List<FileWriterTask> dependentTasks;

    public HostStage(CmdOp cmdOp) {
        super();
        this.cmdOp = cmdOp;
    }

    @Override
    public BoundStage from(ByteSource byteSource) {
        return new HostBoundStage(cmdOp, byteSource);
    }

    @Override
    public BoundStage from(FileWriterTask inputTask) {
        return new HostBoundStage(cmdOp, inputTask);
    }

    @Override
    public BoundStage from(BoundStage execBuilder) {
        return new HostBoundStage(cmdOp, execBuilder);
    }

    @Override
    public BoundStage fromNull() {
        return new HostBoundStage(cmdOp);
    }
}
