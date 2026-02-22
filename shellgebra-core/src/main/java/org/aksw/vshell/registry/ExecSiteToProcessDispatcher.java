package org.aksw.vshell.registry;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ExecutorService;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVisitor;
import org.aksw.shellgebra.algebra.cmd.op.placed.PlacedCmdOp.PlacedCmd;
import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.aksw.shellgebra.exec.model.ExecSite;
import org.aksw.shellgebra.exec.model.ExecSiteCurrentHost;
import org.aksw.shellgebra.exec.model.ExecSiteCurrentJvm;
import org.aksw.shellgebra.exec.model.ExecSiteDockerImage;
import org.aksw.shellgebra.exec.model.ExecSiteVisitor;
import org.aksw.shellgebra.processbuilder.IProcessBuilderCore;

/**
 * A visitor of exec sites that returns corresponding CmdOpVisitors for producing process builders.
 * Any processes started during the dispatch process are registered with this instance.
 */
public class ExecSiteToProcessDispatcher
    implements ExecSiteVisitor<CmdOpVisitorToBase>
{
    private FinalPlacement finalPlacement;
    // private CommandParserCatalog parserCatalog;
    private CommandSiteCatalog commandCatalog;

    private FileMapper fileMapper;
    private ExecutorService executorService;

    private ProcessRunner context;

    private CmdOpVisitorToPbJvm jvmVisitor;
    private CmdOpVisitorToPbHost hostVisitor;

    private ExecSiteResolver resolver;

    private Deque<Process> closeables = new ArrayDeque<>();

    public ExecSiteToProcessDispatcher(FinalPlacement finalPlacement, ProcessRunner context, CommandSiteCatalog commandCatalog, FileMapper fileMapper, ExecSiteResolver resolver, ExecutorService executorService) {
        super();
        this.fileMapper = fileMapper;
        this.context = context;
        // this.parserCatalog = parserCatalog;
        this.commandCatalog = commandCatalog;

        this.finalPlacement = finalPlacement;
        this.executorService = executorService;

        this.resolver = resolver;

        this.jvmVisitor = new CmdOpVisitorToPbJvm(this);
        this.hostVisitor = new CmdOpVisitorToPbHost(this);
    }

    public ExecSiteResolver getResolver() {
        return resolver;
    }


    // XXX Hides the intermediate getResolver() - Perhaps getResolver can be made private.
    public JvmCommandRegistry getJvmCmdRegistry() {
        return getResolver().getJvmCmdRegistry();
    }

    public void addProcess(Process closeable) {
        closeables.add(closeable);
    }

    public Deque<Process> getProcesses() {
        return closeables;
    }

    public ProcessRunner getContext() {
        return context;
    }

    public FileMapper getFileMapper() {
        return fileMapper;
    }

    public CommandSiteCatalog getCommandCatalog() {
        return commandCatalog;
    }

//    public CommandParserCatalog getParserCatalog() {
//        return parserCatalog;
//    }

    @Override
    public CmdOpVisitorToBase visit(ExecSiteDockerImage execSite) {
        return new CmdOpVisitorToPbDocker(this, execSite);
    }

    @Override
    public CmdOpVisitorToBase visit(ExecSiteCurrentHost execSite) {
        return hostVisitor;
    }

    @Override
    public CmdOpVisitorToBase visit(ExecSiteCurrentJvm execSite) {
        return jvmVisitor;
    }

    public IProcessBuilderCore<?> resolve(CmdOpVar v) {
        PlacedCmd placedCmd = finalPlacement.placements().get(v);
        IProcessBuilderCore<?> result = resolve(placedCmd);
        return result;
    }

    public IProcessBuilderCore<?> resolve(PlacedCmd placedCmd) {
        ExecSite execSite = placedCmd.execSite();
        CmdOp cmdOp = placedCmd.cmdOp();
        CmdOpVisitor<IProcessBuilderCore<?>> visitor = execSite.accept(this);
        IProcessBuilderCore<?> result = cmdOp.accept(visitor);
        return result;
    }
}
