package org.aksw.shellgebra.algebra.cmd.arg;

import java.util.Objects;

public record CmdArgPath(String path)
    implements CmdArg
{
    public CmdArgPath {
        Objects.requireNonNull(path);
    }

    @Override
    public <T> T accept(CmdArgVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
