package org.aksw.shellgebra.algebra.cmd.op;

import java.util.List;
import java.util.Objects;

import org.aksw.shellgebra.algebra.cmd.redirect.Redirect;

/** Command group - brace group (not a sub-shell).
 *
 * <pre>
 * { cmd1; ...; cmdN }
 * </pre>
 */
public record CmdOpGroup(List<CmdOp> subOps, List<Redirect> redirects)
    implements CmdOp
{
    public CmdOpGroup {
        Objects.requireNonNull(subOps);
        Objects.requireNonNull(redirects);
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
