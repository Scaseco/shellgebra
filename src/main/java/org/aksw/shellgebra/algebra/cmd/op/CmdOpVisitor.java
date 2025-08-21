package org.aksw.shellgebra.algebra.cmd.op;

public interface CmdOpVisitor<T> {
    T visit(CmdOpExec op);
    // T visit(CmdOpPipe op);
    T visit(CmdOpPipeline op);
    T visit(CmdOpGroup op);
    T visit(CmdOpVar op);
    // T visit(CmdOpSubst op);
    T visit(CmdOpToArg op);
    // T visit(CmdOpRedirectRight op);
}
