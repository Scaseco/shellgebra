package org.aksw.shellgebra.exec.site;

import java.util.List;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.algebra.cmd.op.placed.PlacedCmdOp.PlacedCmd;
import org.aksw.shellgebra.algebra.cmd.transformer.CmdOpTransformBase;
import org.aksw.shellgebra.exec.BoundStage;
import org.aksw.shellgebra.exec.Stage;
import org.aksw.shellgebra.exec.model.ExecSite;
import org.aksw.shellgebra.exec.model.ExecSiteCurrentHost;
import org.aksw.shellgebra.exec.model.ExecSiteCurrentJvm;
import org.aksw.shellgebra.exec.model.ExecSiteDockerImage;
import org.aksw.shellgebra.exec.model.ExecSiteVisitor;
import org.aksw.vshell.registry.FinalPlacement;

// So the resulting expression replaces vars with "cat named-pipe"
// The difficulty is, that if there is a variable in a group, then a named pipe needs to be created for the whole
// group, and each member of that group needs to get the named pipe passed.
// So what is the input to a group? (CmdOp? BoundStage?)
// i think, in a first step, it needs to be a CmdOp expression.
//
class CmdOpTransformStageGenerator {
    private ExecRewriteCxt cxt;

    // @Override
    public CmdOp transform(CmdOpGroup op, BoundStage input) {

        // If there is a var in the group then:
        // (1) If the exec site is jvm, then
        // - allocate a name for an output pipe and create it. e.g. name='outPipe'
        // - allocate a java process entry
        // - emit echo "{ pid=$$, call: [] } > processCtl"; cat outPipe }
        // (2) If the exec site is docker, then
        // - allocate a name for an output pipe and create it. e.g. name='outPipe'
        // - bind-mount
        //   . the input pipe (or the fd???) as input into the container
        //   . the output pipe (it will be used for cat outPipe)
        //  rewrite op into op < in > out

        List<CmdOp> subOps = op.subOps();
        for (CmdOp subOp : subOps) {
            // CmdOp outOp = processSubOp(subOp);
        }

        // If there is a var in the group then:
        // (1) the current input must be passed on to a named pipe and
        // (2) that named pipe must be passed to all members of the group.
        // The var gets substituted for 'cat named-pipe'.

        boolean hasVars = subOps.stream().anyMatch(subOp -> subOp instanceof CmdOpVar);

        if (!hasVars) {
            // Easy case: return as is.
            // return CmdOpTransformBase.super.transform(op, subOps);
        } else {
            // Hard case: turn the

        }
        return null;
    }



    public CmdOp transform(CmdOpPipeline op, BoundStage input) {
        BoundStage result = input;
        List<CmdOp> subOps = op.subOps();
        for (CmdOp subOp : subOps) {
            // subOp.
        }

        // TODO Auto-generated method stub
        // return CmdOpTransformBase.super.transform(op, subOps);
        return null;
    }

    public CmdOp transform(CmdOpVar op, BoundStage input) {
        // TODO Auto-generated method stub
        // return CmdOpTransformBase.super.transform(op);
        return null;
    }
}

// superInput | { x | { y ; VAR } }
class CmdOpTransformStageGenerator2
    implements CmdOpTransformBase
{
    private Stage inputStage;

    @Override
    public CmdOp transform(CmdOpGroup op, List<CmdOp> subOps) {
        // If there is a var in the group then
        // (1) the current input must be passed on to a named pipe and
        // (2) that named pipe must be passed to all members of the group.
        // The var gets substituted for 'cat named-pipe'.

        boolean hasVars = subOps.stream().anyMatch(subOp -> subOp instanceof CmdOpVar);

        if (!hasVars) {
            // Easy case: return as is.
            return CmdOpTransformBase.super.transform(op, subOps);
        } else {
            // Hard case: turn the
        }
        return null;
    }

    @Override
    public CmdOp transform(CmdOpPipeline op, List<CmdOp> subOps) {
        // TODO Auto-generated method stub
        return CmdOpTransformBase.super.transform(op, subOps);
    }

    @Override
    public CmdOp transform(CmdOpVar op) {
        // TODO Auto-generated method stub
        return CmdOpTransformBase.super.transform(op);
    }

    public CmdOp process(CmdOp op) {
        CmdOp result = op instanceof CmdOpVar
            ? processVar(op)
            : op.accept(null); // TODO
        return result;
    }

    public CmdOp processVar(CmdOp opVar) {
        FinalPlacement placements = null;
        PlacedCmd placedCmd = placements.placements().get(opVar);

        ExecSite execSite = placedCmd.execSite();
        CmdOp subCmd = placedCmd.cmdOp();

        // If the sub op is host or docker: create input / output pipes and wire up the containers.
        // If the sub op is jvm: delegate to the jvm handler that injects procCtl+echo procOut statements.
        //   (for docker containers, procCtl must be bind mounted)

        CmdOp result = execSite.accept(new ExecSiteVisitor<CmdOp>() {
            @Override
            public CmdOp visit(ExecSiteDockerImage execSite) {
                throw new UnsupportedOperationException();
            }

            @Override
            public CmdOp visit(ExecSiteCurrentHost execSite) {
                throw new UnsupportedOperationException();
            }

            @Override
            public CmdOp visit(ExecSiteCurrentJvm execSite) {
                throw new UnsupportedOperationException();
            }
        });

        return result;
    }
}
