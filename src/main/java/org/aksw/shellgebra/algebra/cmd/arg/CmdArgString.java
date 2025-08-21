package org.aksw.shellgebra.algebra.cmd.arg;

import java.util.Objects;

public record CmdArgString(String str)
    implements CmdArg
{
    public CmdArgString {
        Objects.requireNonNull(str);
    }

    @Override
    public <T> T accept(CmdArgVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
