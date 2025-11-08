package org.aksw.shellgebra.algebra.cmd.transformer;

import java.util.ArrayList;
import java.util.List;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVisitor;

public class CmdOpVisitorApplyTransform
    implements CmdOpVisitor<CmdOp>
{
    protected CmdOpTransform cmdOpTransform;
    protected CmdArgTransform cmdArgTransform;
    protected TokenTransform tokenTransform;

    public CmdOpVisitorApplyTransform(CmdOpTransform cmdOpTransform, CmdArgTransform cmdArgTransform, TokenTransform tokenTransform) {
        super();
        this.cmdOpTransform = cmdOpTransform;
        this.cmdArgTransform = cmdArgTransform;
        this.tokenTransform = tokenTransform;
    }

    public static List<CmdOp> transformAll(CmdOpVisitor<? extends CmdOp> transform, List<? extends CmdOp> subOps) {
        List<CmdOp> newOps = new ArrayList<>(subOps.size());
        for (CmdOp subOp : subOps) {
            CmdOp newOp = subOp.accept(transform);
            newOps.add(newOp);
        }
        return newOps;
    }

//    public static List<CmdArg> transformAllArgs(CmdOpVisitor<? extends CmdOp> transform, List<? extends CmdArg> inArgs) {
//        List<CmdArg> outArgs = new ArrayList<>(inArgs.size());
//        for (CmdArg inArg : inArgs) {
//            CmdArg outArg = CmdArgTransformer.transform(inArg, cmdArgTransform, this, null); // oldArg.accept(argVisitor);
////            CmdArg newArg = arg instanceof CmdArgCmdOp argOp
////                ? new CmdArgCmdOp(argOp.cmdOp().accept(transform))
////                : arg;
//            outArgs.add(newArg);
//        }
//        return outArgs;
//    }

    @Override
    public CmdOp visit(CmdOpExec op) {
        List<CmdArg> inArgs = op.args().args();
        List<CmdArg> outArgs = new ArrayList<>(inArgs.size());
        for (CmdArg inArg : inArgs) {
            CmdArg outArg = CmdArgTransformer.transform(inArg, cmdArgTransform, cmdOpTransform, tokenTransform);
            outArgs.add(outArg);
        }
        CmdOp result = cmdOpTransform.transform(op, outArgs);
        return result;
    }

    @Override
    public CmdOp visit(CmdOpPipeline op) {
//    	CmdOp subOp1 = op.getSubOp1();
//    	CmdOp subOp2 = op.getSubOp2();
//        CmdOp newOp1 = op.getSubOp1().accept(this);
//        CmdOp newOp2 = op.getSubOp2().accept(this);
        List<CmdOp> newOps = op.getSubOps().stream().map(subOp -> subOp.accept(this)).toList();
        CmdOp result = new CmdOpPipeline(newOps);
//        CmdOp result = transform.transform(op, newOp1, newOp2);
        return result;
    }

    @Override
    public CmdOp visit(CmdOpGroup op) {
        List<CmdOp> newOps = transformAll(this, op.subOps());
        CmdOp result = cmdOpTransform.transform(op, newOps);
        return result;
    }

//    @Override
//    public CmdOp visit(CmdOpString op) {
//        CmdOp result = transform.transform(op);
//        return result;
//    }

//    @Override
//    public CmdOp visit(CmdOpSubst op) {
//        CmdOp subOp = op.getSubOp().accept(this);
//        CmdOp result = transform.transform(op, subOp);
//        return result;
//    }

//    @Override
//    public CmdOp visit(CmdOpToArg op) {
//        CmdOp subOp = op.getSubOp().accept(this);
//        CmdOp result = transform.transform(op, subOp);
//        return result;
//    }

//    @Override
//    public CmdOp visit(CmdOpFile op) {
//        CmdOp result = transform.transform(op);
//        return result;
//    }

//    @Override
//    public CmdOp visit(CmdOpRedirectRight op) {
//        CmdOp newOp = op.getSubOp().accept(this);
//        CmdOp result = transform.transform(op, newOp);
//        return result;
//    }

    @Override
    public CmdOp visit(CmdOpVar op) {
        CmdOp result = cmdOpTransform.transform(op);
        return result;
    }
}
