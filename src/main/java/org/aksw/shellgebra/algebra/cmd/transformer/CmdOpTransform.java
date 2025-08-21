package org.aksw.shellgebra.algebra.cmd.transformer;

import java.util.List;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpToArg;

public interface CmdOpTransform
{
    CmdOp transform(CmdOpPipeline op, List<CmdOp> subOps);
    CmdOp transform(CmdOpGroup op, List<CmdOp> subOps);
    // CmdOp transform(CmdOpSubst op, CmdOp subOp);
    CmdOp transform(CmdOpExec op, List<CmdArg> subOps);
    CmdOp transform(CmdOpToArg op, CmdOp subOp);
//    CmdOp transform(CmdOpString op);
//    CmdOp transform(CmdOpFile op);
//     CmdOp transform(CmdOpRedirectRight op, CmdOp subOp);
}
