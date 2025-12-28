package org.aksw.shellgebra.algebra.cmd.arg;

public interface CmdArgVisitor<T> {
    T visit(CmdArgCmdOp arg);
    T visit(CmdArgRedirect arg);
    T visit(CmdArgWord arg);
}
