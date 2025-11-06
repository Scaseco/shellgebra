package org.aksw.shellgebra.algebra.cmd.op;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A pipe operator as known from bash.
 *
 * <pre>
 * foo | bar | ... | baz
 * </pre>
 */
public record CmdOpPipeline(List<CmdOp> subOps)
    implements CmdOpN
{
    public CmdOpPipeline {
        subOps = Objects.requireNonNull(subOps);
    }

    @SafeVarargs
    public <T extends CmdOp> CmdOpPipeline(T ...subOps) {
        this(List.of(subOps));
    }

    @Override
    public List<CmdOp> getSubOps() {
        return subOps;
    }

    @Override
    public <T> T accept(CmdOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    @Override
    public String toString() {
        return "(pipeline " + getSubOps().stream().map(Objects::toString).collect(Collectors.joining(" ")) + ")";
    }
}
