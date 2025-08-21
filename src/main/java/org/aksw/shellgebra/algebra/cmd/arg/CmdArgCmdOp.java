package org.aksw.shellgebra.algebra.cmd.arg;

import java.util.Objects;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;

/** Process substitution &lt;(cmdOp) */
public record CmdArgCmdOp(CmdOp cmdOp)
    implements CmdArg
{
    public CmdArgCmdOp {
        Objects.requireNonNull(cmdOp);
    }

    @Override
    public <T> T accept(CmdArgVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}

