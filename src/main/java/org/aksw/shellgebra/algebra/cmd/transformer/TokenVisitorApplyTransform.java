package org.aksw.shellgebra.algebra.cmd.transformer;

import org.aksw.shellgebra.algebra.cmd.arg.Token;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenCmdOp;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenLiteral;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenPath;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenVar;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenVisitor;

public class TokenVisitorApplyTransform
    implements TokenVisitor<Token>
{
    private TokenTransform transform;
    private CmdOpTransform cmdOpTransform;

    @Override
    public Token visit(TokenLiteral token) {
        Token result = transform.transform(token);
        return result;
    }

    @Override
    public Token visit(TokenPath token) {
        Token result = transform.transform(token);
        return result;
    }

    @Override
    public Token visit(TokenVar token) {
        Token result = transform.transform(token);
        return result;
    }

    @Override
    public Token visit(TokenCmdOp token) {
    	
        // CmdOp newCmdOp = cmdOpTransform.tr

        Token result = transform.transform(token);
        return result;
    }
}
