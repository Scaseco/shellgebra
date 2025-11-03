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
