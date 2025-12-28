package org.aksw.shellgebra.algebra.cmd.transformer;

import java.util.List;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgCmdOp;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgRedirect;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgWord;
import org.aksw.shellgebra.algebra.cmd.arg.Token;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;

public interface CmdArgTransform {
    CmdArg transform(CmdArgWord arg, List<Token> subTokens);
    CmdArg transform(CmdArgCmdOp arg, CmdOp subOp);
    CmdArg transform(CmdArgRedirect arg);
}
