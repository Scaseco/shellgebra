package org.aksw.vshell.registry;

import java.util.List;
import java.util.function.Function;

import org.aksw.commons.util.list.ListUtils;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVisitor;
import org.aksw.shellgebra.exec.PipelineStage;
import org.aksw.shellgebra.exec.Stage;
import org.aksw.shellgebra.exec.Stages;
import org.aksw.shellgebra.exec.io.StageGroup;

public class CmdOpVisitorExecJvm
    implements CmdOpVisitor<Stage>
{
    private Function<CmdOpVar, Stage> varResolver;

    public CmdOpVisitorExecJvm(Function<CmdOpVar, Stage> varResolver) {
        super();
        this.varResolver = varResolver;
    }

    @Override
    public Stage visit(CmdOpExec op) {
        Stage result = Stages.host(op);
        return result;
    }

    protected List<Stage> toStages(List<CmdOp> ops) {
        List<Stage> result = ListUtils.map(ops, op -> op.accept(this));
        return result;
    }

    @Override
    public Stage visit(CmdOpPipeline op) {
        List<CmdOp> subOps = op.getSubOps();
        List<Stage> stages = toStages(subOps);
        Stage result = stages.size() == 1
            ? stages.get(0)
            : new PipelineStage(stages);
        return result;
    }

    @Override
    public Stage visit(CmdOpGroup op) {
        List<CmdOp> subOps = op.subOps();
        List<Stage> stages = toStages(subOps);
        Stage result = new StageGroup(stages);
        return result;
    }

    @Override
    public Stage visit(CmdOpVar op) {
        Stage result = varResolver.apply(op);
        return result;
    }
}
