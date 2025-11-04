package org.aksw.shellgebra.algebra.cmd.transformer;

import java.util.Objects;

import org.aksw.shellgebra.algebra.cmd.arg.Token;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenCmdOp;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenLiteral;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenPath;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenVar;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenVisitor;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;

public class TokenVisitorApplyTransform
    implements TokenVisitor<Token>
{
    private TokenTransform tokenTransform;
    private CmdOpTransform cmdOpTransform;
    private CmdArgTransform cmdArgTransform;

    public TokenVisitorApplyTransform(TokenTransform transform, CmdOpTransform cmdOpTransform, CmdArgTransform cmdArgTransform) {
        super();
        this.tokenTransform = Objects.requireNonNull(transform);
        this.cmdOpTransform = Objects.requireNonNull(cmdOpTransform);
        this.cmdArgTransform = Objects.requireNonNull(cmdArgTransform);
    }

    @Override
    public Token visit(TokenLiteral token) {
        Token result = tokenTransform.transform(token);
        return result;
    }

    @Override
    public Token visit(TokenPath token) {
        Token result = tokenTransform.transform(token);
        return result;
    }

    @Override
    public Token visit(TokenVar token) {
        Token result = tokenTransform.transform(token);
        return result;
    }

    @Override
    public Token visit(TokenCmdOp token) {
        CmdOp inCmdOp = token.cmdOp();
        CmdOp outCmdOp = CmdOpTransformer.transform(inCmdOp, cmdOpTransform, cmdArgTransform, tokenTransform);
        Token result = tokenTransform.transform(token, outCmdOp);
        return result;
    }
}
