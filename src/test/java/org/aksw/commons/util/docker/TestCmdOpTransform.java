package org.aksw.commons.util.docker;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.arg.Token;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenPath;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.transformer.CmdOpTransformer;
import org.aksw.shellgebra.algebra.cmd.transformer.TokenTransform;
import org.aksw.shellgebra.algebra.cmd.transformer.TokenTransformBase;
import org.junit.Test;

public class TestCmdOpTransform {
    @Test
    public void test01() {
        CmdOp cmdOp = new CmdOpExec("/test", CmdArg.ofLiteral("-i"), CmdArg.ofPathString("/foo"));
        TokenTransform tokenTransform = new TokenTransformBase() {
            @Override
            public Token transform(TokenPath token) {
                return new TokenPath("/bar" + token.path());
            }
        };
        CmdOp newCmdOp = CmdOpTransformer.transform(cmdOp, null, null, tokenTransform);
        System.out.println(newCmdOp);
    }
}
