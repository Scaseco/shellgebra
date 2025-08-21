package org.aksw.shellgebra.algebra.cmd.transformer;

import java.util.List;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpToArg;

public class CmdOpTransformBase
    implements CmdOpTransform
{
    @Override
    public CmdOp transform(CmdOpPipeline op, List<CmdOp> subOps) {
        return new CmdOpPipeline(subOps);
    }

    @Override
    public CmdOp transform(CmdOpGroup op, List<CmdOp> subOps) {
        return new CmdOpGroup(subOps, op.redirects());
    }

    @Override
    public CmdOp transform(CmdOpExec op, List<CmdArg> subOps) {
        return new CmdOpExec(op.getName(), subOps, op.redirects());
    }

//    @Override
//    public CmdOp transform(CmdOpSubst op, CmdOp subOp) {
//        return new CmdOpSubst(subOp);
//    }

    @Override
    public CmdOp transform(CmdOpToArg op, CmdOp subOp) {
        return new CmdOpToArg(subOp);
    }

//    @Override
//    public CmdOp transform(CmdOpString op) {
//        return op;
//        // return new CmdOpString(op.value);
//    }
//
//    @Override
//    public CmdOp transform(CmdOpFile op) {
//        return op;
//        // return new CmdOpString(op.value);
//    }

//    @Override
//    public CmdOp transform(CmdOpRedirectRight op, CmdOp subOp) {
//        return new CmdOpRedirectRight(op.getFileName(), subOp);
//    }
}
