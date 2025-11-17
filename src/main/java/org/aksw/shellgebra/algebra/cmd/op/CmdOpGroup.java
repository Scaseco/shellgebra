package org.aksw.shellgebra.algebra.cmd.op;

import java.util.List;
import java.util.Objects;

import org.aksw.shellgebra.algebra.cmd.redirect.CmdRedirect;

/**
 * Command group - brace group (not a sub-shell).
 *
 * <pre>
 * { cmd1; ...; cmdN }
 * </pre>
 */
public record CmdOpGroup(List<CmdOp> subOps, List<CmdRedirect> redirects)
    implements CmdOp
{
    public CmdOpGroup {
        Objects.requireNonNull(subOps);
        Objects.requireNonNull(redirects);
    }

    public CmdOpGroup(List<CmdOp> subOps) {
        this(subOps, List.of());
    }

    public static CmdOpGroup of(List<CmdOp> subOps, List<CmdRedirect> redirects) {
        return new CmdOpGroup(subOps, redirects);
    }

    public static CmdOpGroup of(CmdOp ...subOps) {
        return new CmdOpGroup(List.of(subOps), List.of());
    }

    @Override
    public <T> T accept(CmdOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    @Override
    public String toString() {
        return "(group " + CmdOp.toStrings(subOps, redirects) + ")";
    }
}
