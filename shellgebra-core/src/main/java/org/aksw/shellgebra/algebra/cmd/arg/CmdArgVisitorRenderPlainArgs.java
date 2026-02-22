package org.aksw.shellgebra.algebra.cmd.arg;

import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenCmdOp;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenLiteral;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenPath;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenVar;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenVisitor;

/**
 * Attempt to converts a CmdArg into a plain string. Fails with {@link IllegalArgumentException}
 * if the CmdArg instance is not a plain string or file.
 *
 * Use {@link CmdArgVisitorRenderAsBashString} to handle command substitution, process substitution, redirects, etc.
 */
public class CmdArgVisitorRenderPlainArgs
    implements CmdArgVisitor<String>
{
    private static final CmdArgVisitor<String> INSTANCE = new CmdArgVisitorRenderPlainArgs();

    public static CmdArgVisitor<String> get() {
        return INSTANCE;
    }

    private CmdArgVisitorRenderPlainArgs() {
    }

    @Override
    public String visit(CmdArgWord arg) {
        TokenVisitor<String> renderer = new TokenVisitorRenderToString();
        StringBuilder sb = new StringBuilder();
        for (Token token : arg.tokens()) {
            String contrib = token.accept(renderer);
            sb.append(contrib);
        }
        String result = sb.toString();
        return result;
    }

    /** Unquoted command substitution: {@code cmd $(generate_args)}*/
    @Override
    public String visit(CmdArgCmdOp arg) {
        throw new IllegalArgumentException("Cannot create plain argv strings because encountered: Unquoted command substitution.");
    }

    @Override
    public String visit(CmdArgRedirect arg) {
        throw new IllegalArgumentException("Cannot create plain argv strings because encountered: Redirect.");
    }

    public class TokenVisitorRenderToString
        implements TokenVisitor<String>
    {
        @Override
        public String visit(TokenLiteral token) {
            return token.value();
        }

        @Override
        public String visit(TokenPath token) {
            return token.path();
        }

        @Override
        public String visit(TokenVar token) {
            throw new IllegalArgumentException("Cannot create plain argv strings because encountered: Variable.");
        }

        @Override
        public String visit(TokenCmdOp token) {
            throw new IllegalArgumentException("Cannot create plain argv strings because encountered: Process Substitution.");
        }
    }
}
