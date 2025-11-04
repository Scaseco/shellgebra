package org.aksw.shellgebra.algebra.cmd.transformer;

import java.util.List;

import org.aksw.commons.collections.IterableUtils;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;

public class CmdOpTransformBase
    implements CmdOpTransform
{
    @Override
    public CmdOp transform(CmdOpPipeline op, List<CmdOp> subOps) {
        CmdOp result = (IterableUtils.equalsByReference(subOps, op.getSubOps()))
            ? op
            : new CmdOpPipeline(subOps);
        return result;
    }

    @Override
    public CmdOp transform(CmdOpGroup op, List<CmdOp> subOps) {
        CmdOp result = (IterableUtils.equalsByReference(subOps, op.subOps()))
            ? op
            : new CmdOpGroup(subOps, op.redirects());
        return result;
    }

    @Override
    public CmdOp transform(CmdOpExec op, List<CmdArg> subOps) {
        CmdOp result = (IterableUtils.equalsByReference(subOps, op.getArgs()))
            ? op
            : new CmdOpExec(op.getName(), subOps, op.redirects());
        return result;
    }

    @Override
    public CmdOp transform(CmdOpVar op) {
        return op;
    }
}
