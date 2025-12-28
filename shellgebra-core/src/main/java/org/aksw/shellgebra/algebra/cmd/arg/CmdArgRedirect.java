package org.aksw.shellgebra.algebra.cmd.arg;

import java.util.Objects;

import org.aksw.shellgebra.algebra.cmd.redirect.CmdRedirect;

/**
 * CmdArgRedirect should probably be avoided in favor of explicit redirects
 * on CmdOpExec.
 *
 * <pre>
 * Cmd &lt; file
 * Cmd &gt; file
 * Cmd &gt;&gt; file
 * </pre>
 *
 * inputExpr must produce output - should be file or process.
 */
public record CmdArgRedirect(CmdRedirect redirect)
    implements CmdArg
{
    public CmdArgRedirect {
        Objects.requireNonNull(redirect);
    }

    @Override
    public <T> T accept(CmdArgVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
