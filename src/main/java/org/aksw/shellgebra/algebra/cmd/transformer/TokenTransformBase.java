package org.aksw.shellgebra.algebra.cmd.transformer;

import org.aksw.shellgebra.algebra.cmd.arg.Token;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenCmdOp;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenLiteral;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenPath;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenVar;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;

public class TokenTransformBase
    implements TokenTransform
{
    @Override
    public Token transform(TokenLiteral token) {
        return token;
    }

    @Override
    public Token transform(TokenPath token) {
        return token;
    }

    @Override
    public Token transform(TokenCmdOp token, CmdOp subOp) {
        Token result = (token.cmdOp() == subOp)
            ? token
            : new TokenCmdOp(subOp);
        return result;
    }

    @Override
    public Token transform(TokenVar token) {
        return token;
    }
}
