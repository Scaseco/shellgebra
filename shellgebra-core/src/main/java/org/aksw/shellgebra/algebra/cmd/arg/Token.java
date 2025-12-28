package org.aksw.shellgebra.algebra.cmd.arg;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;

public sealed interface Token {

    public interface TokenVisitor<T> {
        T visit(TokenLiteral token);
        T visit(TokenPath token);
        T visit(TokenVar token);
        T visit(TokenCmdOp token);
    }

    <T> T accept(TokenVisitor<T> visitor);

    public record TokenLiteral(String value) implements Token {
        @Override
        public <T> T accept(TokenVisitor<T> visitor) {
            T result = visitor.visit(this);
            return result;
        }
    }

    /**
     * XXX Extend with optional read/write flags.
     * XXX Extend with flag whether this path is relative to a container - as to remapping paths already mapped to a container?
     *       Alternatively, this could be partly detected via fileMapper lookups (i.e. is this a mapped path?)
     */
    public record TokenPath(String path) implements Token {
        @Override
        public <T> T accept(TokenVisitor<T> visitor) {
            T result = visitor.visit(this);
            return result;
        }
    }

    public record TokenVar(String name) implements Token {
        @Override
        public <T> T accept(TokenVisitor<T> visitor) {
            T result = visitor.visit(this);
            return result;
        }
    }

    // This is string interpolation - not to be confused with process substition.
    // Notes:
    public record TokenCmdOp(CmdOp cmdOp) implements Token {
        @Override
        public <T> T accept(TokenVisitor<T> visitor) {
            T result = visitor.visit(this);
            return result;
        }
    }

    // record TokenStringQuote(String value) implements Token {}
    // record TokenStringDoubleQuote(String value) implements Token {}
}
