package org.aksw.shellgebra.algebra.cmd.transformer;

import org.aksw.shellgebra.algebra.cmd.arg.Token;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenCmdOp;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenLiteral;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenPath;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenVar;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;

public interface TokenTransformBase
    extends TokenTransform
{
    @Override
    default Token transform(TokenLiteral token) {
        return token;
    }

    @Override
    default Token transform(TokenPath token) {
        return token;
    }

    @Override
    default Token transform(TokenCmdOp token, CmdOp subOp) {
        Token result = (token.cmdOp() == subOp)
            ? token
            : new TokenCmdOp(subOp);
        return result;
    }

    @Override
    default Token transform(TokenVar token) {
        return token;
    }
}
