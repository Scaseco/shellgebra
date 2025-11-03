package org.aksw.shellgebra.algebra.cmd.transformer;

import java.util.List;

import org.aksw.commons.collections.IterableUtils;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgCmdOp;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgRedirect;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgWord;
import org.aksw.shellgebra.algebra.cmd.arg.Token;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;

public class CmdArgTransformBase
    implements CmdArgTransform
{
    @Override
    public CmdArg transform(CmdArgWord arg, List<Token> subTokens) {
        CmdArg result = IterableUtils.equalsByReference(arg.tokens(), subTokens)
            ? arg
            : new CmdArgWord(arg.escapeType(), subTokens);
        return result;
    }

    @Override
    public CmdArg transform(CmdArgCmdOp arg, CmdOp subOp) {
        CmdArg result = (arg.cmdOp() == subOp)
            ? arg
            :new CmdArgCmdOp(subOp);
        return result;
    }

    @Override
    public CmdArg transform(CmdArgRedirect arg) {
        return arg;
    }
}
