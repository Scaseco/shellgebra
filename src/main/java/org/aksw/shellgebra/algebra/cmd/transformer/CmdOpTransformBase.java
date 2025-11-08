package org.aksw.shellgebra.algebra.cmd.transformer;

import java.util.List;

import org.aksw.commons.collections.IterableUtils;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.vshell.shim.rdfconvert.ArgumentList;

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
        CmdOp result = (IterableUtils.equalsByReference(subOps, op.args().args()))
            ? op
            : new CmdOpExec(op.getName(), new ArgumentList(subOps)); //, op.redirects());
        return result;
    }

    @Override
    public CmdOp transform(CmdOpVar op) {
        return op;
    }
}
