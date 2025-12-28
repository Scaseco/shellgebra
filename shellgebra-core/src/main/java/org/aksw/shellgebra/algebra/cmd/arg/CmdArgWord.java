package org.aksw.shellgebra.algebra.cmd.arg;

import java.util.List;
import java.util.Objects;

/**
 * A single conventional argument.
 * It represents a string that may be formed from a (non-empty) list of tokens.
 */
public record CmdArgWord(StringEscapeType escapeType, List<Token> tokens)
    implements CmdArg
{
    public CmdArgWord {
        escapeType = Objects.requireNonNull(escapeType);
        tokens = Objects.requireNonNull(tokens);
    }

    public CmdArgWord(StringEscapeType escapeType, Token token) {
        this(escapeType, List.of(Objects.requireNonNull(token)));
    }

    @Override
    public <T> T accept(CmdArgVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
