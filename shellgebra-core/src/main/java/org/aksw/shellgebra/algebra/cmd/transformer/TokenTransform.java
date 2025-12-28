package org.aksw.shellgebra.algebra.cmd.transformer;

import org.aksw.shellgebra.algebra.cmd.arg.Token;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenCmdOp;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenLiteral;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenPath;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenVar;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;

public interface TokenTransform {
    Token transform(TokenLiteral token);
    Token transform(TokenPath token);
    Token transform(TokenCmdOp token, CmdOp subOp);
    Token transform(TokenVar token);
}
