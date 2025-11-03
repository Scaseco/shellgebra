package org.aksw.shellgebra.algebra.cmd.transformer;

import java.util.List;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;

public interface CmdOpTransform
{
    CmdOp transform(CmdOpPipeline op, List<CmdOp> subOps);
    CmdOp transform(CmdOpGroup op, List<CmdOp> subOps);
    // CmdOp transform(CmdOpSubst op, CmdOp subOp);
    CmdOp transform(CmdOpExec op, List<CmdArg> subOps);

    // CmdArg transform(CmdArgWord arg, List<Token> subTokens);
    // Token transform(Token token);

    // CmdOp transform(CmdOpToArg op, CmdOp subOp);

    CmdOp transform(CmdOpVar op);
//    CmdOp transform(CmdOpString op);
//    CmdOp transform(CmdOpFile op);
//     CmdOp transform(CmdOpRedirectRight op, CmdOp subOp);
}
