package org.aksw.shellgebra.algebra.cmd.arg;

import java.util.Objects;

@Deprecated
public record CmdArgLiteral(String str)
    implements CmdArg
{
    public CmdArgLiteral {
        Objects.requireNonNull(str);
    }

    @Override
    public <T> T accept(CmdArgVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
