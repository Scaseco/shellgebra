package org.aksw.shellgebra.algebra.stream.op;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;

// Logical operator - does not include bind mounts.
//public class StreamOpDockerizedCommand
//    extends StreamOp0
//{
//    protected CmdOp cmdOp;
//
//    public StreamOpCommand(CmdOp cmdOp) {
//        super();
//        this.cmdOp = cmdOp;
//    }
//
//    public CmdOp getCmdOp() {
//        return cmdOp;
//    }
//
//    @Override
//    public <T> T accept(StreamOpDockerizedCommand<T> visitor) {
//        T result = visitor.visit(this);
//        return result;
//    }
//
//    @Override
//    public String toString() {
//        return "(cmd " + cmdOp + ")";
//    }
//}
