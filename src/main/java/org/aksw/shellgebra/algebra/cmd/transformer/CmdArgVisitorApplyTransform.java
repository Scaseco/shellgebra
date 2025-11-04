package org.aksw.shellgebra.algebra.cmd.transformer;

import java.util.ArrayList;
import java.util.List;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgCmdOp;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgLiteral;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgPath;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgRedirect;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgString;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgVisitor;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgWord;
import org.aksw.shellgebra.algebra.cmd.arg.Token;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;

public class CmdArgVisitorApplyTransform
    implements CmdArgVisitor<CmdArg>
{
    private CmdArgTransform cmdArgTransform;
    private CmdOpTransform cmdOpTransform;
    private TokenTransform tokenTransform;

    public CmdArgVisitorApplyTransform(CmdArgTransform cmdArgTransform, CmdOpTransform cmdOpTransform,
            TokenTransform tokenTransform) {
        super();
        this.cmdArgTransform = cmdArgTransform;
        this.cmdOpTransform = cmdOpTransform;
        this.tokenTransform = tokenTransform;
    }

    @Override
    public CmdArg visit(CmdArgCmdOp arg) {
        CmdOp inCmdOp = arg.cmdOp();
        CmdOp outCmdOp = CmdOpTransformer.transform(inCmdOp, cmdOpTransform, cmdArgTransform, tokenTransform);
        CmdArg result = cmdArgTransform.transform(arg, outCmdOp);
        return result;
    }

    @Override
    public CmdArg visit(CmdArgRedirect arg) {
        CmdArg result = cmdArgTransform.transform(arg);
        return result;
    }

    @Override
    public CmdArg visit(CmdArgWord arg) {
        List<Token> inTokens = arg.tokens();
        List<Token> outTokens = new ArrayList<>(inTokens.size());
        for (Token inToken : inTokens) {
            Token outToken = TokenTransformer.transform(inToken, tokenTransform, cmdOpTransform, cmdArgTransform);
            outTokens.add(outToken);
        }
        CmdArg result = cmdArgTransform.transform(arg, outTokens);
        return result;
    }

    @Override
    public CmdArg visit(CmdArgLiteral arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CmdArg visit(CmdArgString arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CmdArg visit(CmdArgPath arg) {
        throw new UnsupportedOperationException();
    }
}
