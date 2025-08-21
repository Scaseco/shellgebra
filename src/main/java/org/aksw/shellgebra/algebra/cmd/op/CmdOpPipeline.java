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
public class CmdOpPipeline
    extends CmdOpN
{
    @SafeVarargs
    public <T extends CmdOp> CmdOpPipeline(T ...subOps) {
        super(List.of(subOps));
    }

    public CmdOpPipeline(List<CmdOp> subOps) {
        super(subOps);
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
