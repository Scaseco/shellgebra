package org.aksw.vshell.registry;

import java.util.ArrayList;
import java.util.List;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgVisitor;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgVisitorRenderAsBashString;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVisitor;
import org.aksw.shellgebra.algebra.cmd.transformer.CmdArgTransform;
import org.aksw.shellgebra.exec.ListBuilder;
import org.aksw.shellgebra.processbuilder.IProcessBuilderCore;
import org.aksw.shellgebra.processbuilder.ProcessBuilderGroup;
import org.aksw.shellgebra.processbuilder.ProcessBuilderPipeline;

public abstract class CmdOpVisitorToBase
    implements CmdOpVisitor<IProcessBuilderCore<?>>
{
    private ExecSiteToProcessDispatcher dispatcher;

    // This is the bridge from cmdArgTransformer to *this* CmdOpVisitor.
    private CmdArgVisitor<CmdArg> cmdArgTransformer;

    public CmdOpVisitorToBase(ExecSiteToProcessDispatcher dispatcher) {
        super();
        this.dispatcher = dispatcher;
        this.cmdArgTransformer = new CmdArgTransformToProcess(this);
    }

    public ExecSiteToProcessDispatcher getDispatcher() {
        return dispatcher;
    }

    public CmdArgVisitor<CmdArg> getCmdArgTransformer() {
        return cmdArgTransformer;
    }

    protected abstract IProcessBuilderCore<?> toProcessBuilder(List<String> args);

    @Override
    public IProcessBuilderCore<?> visit(CmdOpExec op) {
        List<CmdArg> args = op.args().args();

        // TODO Deal with arguments that make use of process substitution.
        List<CmdArg> resolvedArgs = CmdArgTransform.transformArgs(cmdArgTransformer, args);
        List<String> resolvedArgStrs = CmdArgVisitorRenderAsBashString.render(resolvedArgs);

        List<String> argv = ListBuilder.ofString().add(op.getName()).addAll(resolvedArgStrs).buildList();
        IProcessBuilderCore<?> result = toProcessBuilder(argv);
        return result;
    }

    @Override
    public IProcessBuilderCore<?> visit(CmdOpPipeline op) {
        List<CmdOp> subOps = op.subOps();
        List<IProcessBuilderCore<?>> list = new ArrayList<>(subOps.size());
        for (CmdOp subOp : subOps) {
            IProcessBuilderCore<?> newSubOp = subOp.accept(this);
            list.add(newSubOp);
        }
        return ProcessBuilderPipeline.of(list);
    }

    // TODO: If the a group redirect goes to a named pipe then:
    // (1) allocate an anon pipe
    // (2) do: cat anon-pipe > named-pipe (so the write end of the named-pipe receives all data from the anon-pipe)
    // (3) for each group member: set the anon pipe as the redirect target.
    @Override
    public IProcessBuilderCore<?> visit(CmdOpGroup op) {

        // List<? extends IProcessBuilderCore<?>> list = op.subOps().stream().map(subOp -> subOp.accept(this)).toList();
        List<CmdOp> subOps = op.subOps();
        List<IProcessBuilderCore<?>> list = new ArrayList<>(subOps.size());
        for (CmdOp subOp : subOps) {
            IProcessBuilderCore<?> newSubOp = subOp.accept(this);
            list.add(newSubOp);
        }
        return ProcessBuilderGroup.of(list);
    }

    @Override
    public IProcessBuilderCore<?> visit(CmdOpVar op) {
        IProcessBuilderCore<?> result = dispatcher.resolve(op);
        return result;
    }
}
