package org.aksw.vshell.registry;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.aksw.commons.util.list.ListUtils;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgVisitorRenderAsBashString;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVisitor;
import org.aksw.shellgebra.exec.Stage;
import org.aksw.shellgebra.exec.StagePipeline;
import org.aksw.shellgebra.exec.io.StageGroup;
import org.aksw.shellgebra.exec.model.ExecSites;
import org.aksw.vshell.shim.rdfconvert.ArgumentList;

public class CmdOpVisitorExecJvm
    implements CmdOpVisitor<Stage>
{
    // private JvmCommandRegistry jvmCmdRegistry;
    private ExecSiteResolver resolver;
    private Function<CmdOpVar, Stage> varResolver;

    public CmdOpVisitorExecJvm(ExecSiteResolver resolver, Function<CmdOpVar, Stage> varResolver) {
        super();
        // this.jvmCmdRegistry = jvmCmdRegistry;
        this.resolver = resolver;
        this.varResolver = varResolver;
    }

//    public CmdOpVisitorExecJvm(JvmCommandRegistry jvmCmdRegistry, Function<CmdOpVar, Stage> varResolver) {
//        super();
//        this.jvmCmdRegistry = jvmCmdRegistry;
//        this.varResolver = varResolver;
//    }

    @Override
    public Stage visit(CmdOpExec op) {
        String cmdName = op.getName();
//        Set<String> resolvedCmdNames = resolver.getCommandCatalog().get(cmdName, ExecSites.jvm()).orElse(null);
//        if (resolvedCmdNames.isEmpty()) {
//        	throw new RuntimeException("Could not resolve: " + op);
//        }
        JvmCommandRegistry jvmCmdRegistry = resolver.getJvmCmdRegistry();
        String resolvedCmdName = resolver.resolve(cmdName, ExecSites.jvm())
            .orElseThrow(() -> new RuntimeException("Failed to resolved: " + cmdName));

        // TODO In general, resolve any expressions in arguments.
        ArgumentList args = op.args();
        List<String> strs = CmdArgVisitorRenderAsBashString.render(args.args());
        // jvmCmdRegistry.require(cmdName)
        String[] strArr = strs.toArray(String[]::new);
        Stage result = jvmCmdRegistry.newStage(resolvedCmdName, strArr);
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
            : new StagePipeline(stages);
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
        Stage result = varResolver == null ? null : varResolver.apply(op);
        Objects.requireNonNull(result, "Could not resolve variable: " + op);
        return result;
    }
}
