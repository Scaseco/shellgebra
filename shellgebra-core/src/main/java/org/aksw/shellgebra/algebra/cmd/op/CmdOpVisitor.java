package org.aksw.shellgebra.algebra.cmd.op;

public interface CmdOpVisitor<T> {
    T visit(CmdOpExec op);
    T visit(CmdOpPipeline op);
    T visit(CmdOpGroup op);
    T visit(CmdOpVar op);
}
