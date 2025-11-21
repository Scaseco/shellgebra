package org.aksw.shellgebra.exec.site;

import java.util.ArrayList;
import java.util.List;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVisitor;
import org.aksw.shellgebra.algebra.cmd.op.CmdOps;
import org.aksw.shellgebra.algebra.cmd.redirect.CmdRedirect;

/**
 * Sub processor for executing commands on the jvm.
 */
class CmdOpSubOpProcessorJvm
    implements CmdOpVisitor<CmdOp>
{
    private ExecRewriteCxt cxt;

    // (1) If the exec site is jvm, then
    // - allocate a name for an output pipe and create it. e.g. name='outPipe'
    // - allocate a java process entry
    // - emit echo "{ pid=$$, call: [] } > processCtl"; cat outPipe }
    @Override
    public CmdOp visit(CmdOpExec op) {
        int cmdId = cxt.processCounter++;
        String outPipePath = cxt.allocateOutPipePath(cmdId);
        CmdOp notifyProcCtlCmdOp = CmdOps.appendRedirect(
            CmdOpExec.ofLiterals("echo", "{ pid: \"$$\", cmdId: \"" + cmdId + "\"}"),
            CmdRedirect.out(cxt.processCtlPath));

        // Register op for execution via the jvm once the event on the procCtl channel is seen.
        ExecStage execStage = new ExecStage(op);
        cxt.externalExecutions.put(outPipePath, execStage);

        CmdOp catProcOutcmdOp = CmdOpExec.ofLiteralArgs("cat", outPipePath);
        CmdOp result = CmdOps.group(notifyProcCtlCmdOp, catProcOutcmdOp);
        return result;
    }

    @Override
    public CmdOp visit(CmdOpPipeline op) {
        List<CmdOp> inSubOps = op.subOps();
        List<CmdOp> outSubOps = new ArrayList<>(inSubOps.size());
        for (CmdOp inSubOp : inSubOps) {
            CmdOp outSubOp = inSubOp.accept(this);
            outSubOps.add(outSubOp);
        }
        return CmdOps.pipeline(outSubOps);
    }

    @Override
    public CmdOp visit(CmdOpGroup op) {
        List<CmdOp> inSubOps = op.subOps();
        List<CmdOp> outSubOps = new ArrayList<>(inSubOps.size());
        for (CmdOp inSubOp : inSubOps) {
            CmdOp outSubOp = inSubOp.accept(this);
            outSubOps.add(outSubOp);
        }
        return CmdOps.group(outSubOps, op.redirects());
    }

    @Override
    public CmdOp visit(CmdOpVar op) {
        throw new RuntimeException("not implemented yet.");
    }
}
