package org.aksw.vshell.registry;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Set;

import org.aksw.commons.util.docker.ImageIntrospectorImpl;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgCmdOp;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgVisitor;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.placed.PlacedCmdOp.PlacedCmd;
import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.aksw.shellgebra.exec.model.ExecSite;
import org.aksw.shellgebra.exec.model.ExecSiteCurrentHost;
import org.aksw.shellgebra.exec.model.ExecSites;
import org.aksw.shellgebra.exec.model.PlacedCommand;
import org.aksw.shellgebra.model.osreo.ImageIntrospector;
import org.aksw.shellgebra.registry.init.InitCommandRegistry;

/**
 * The default command rewrite and execution system.
 * Technically a facade over command catalogs for different execution sites (host, docker, jvm).
 */
public class CmdExecSystem {
    private JvmCommandRegistry jvmCmdRegistry;
    private CommandRegistry candidates;

    private CommandCatalog cmdCatalog;

    private CommandRegistry inferredCatalog;
    private CommandSiteCatalog hostCatalog;
    private CommandSiteCatalog jvmCatalog;
    private CommandSiteCatalog unionCatalog;

    private ExecSiteProbeResults probeResults;
    // TODO Have image introspector write into cmdAvailability without having to know about exec sites.
    // Need an adapter or cmdAvailability.asDockerImageMap().

    // Model shellModel = RDFDataMgr.loadModel("shell-ontology.ttl");
    private ImageIntrospector imageIntrospector; // shellModel, probeResults);
    // imageIntrospector = new ImageIntrospectorCaching(imageIntrospector);

    private ExecSiteResolver resolver;

    /** Use {@link #newBuilder()} to create instances. */
//    CmdExecSystem() {
//        super();
//    }
//
    public JvmCommandRegistry getJvmCmdRegistry() {
        return jvmCmdRegistry;
    }

    public CmdExecSystem(CommandCatalog cmdCatalog, JvmCommandRegistry jvmCmdRegistry, CommandRegistry candidates, CommandRegistry inferredCatalog,
            CommandSiteCatalog hostCatalog, CommandSiteCatalog jvmCatalog, CommandSiteCatalog unionCatalog,
            ExecSiteProbeResults probeResults, ImageIntrospector imageIntrospector, ExecSiteResolver resolver) {
        super();
        this.cmdCatalog = cmdCatalog;
        this.jvmCmdRegistry = jvmCmdRegistry;
        this.candidates = candidates;
        this.inferredCatalog = inferredCatalog;
        this.hostCatalog = hostCatalog;
        this.jvmCatalog = jvmCatalog;
        this.unionCatalog = unionCatalog;
        this.probeResults = probeResults;
        this.imageIntrospector = imageIntrospector;
        this.resolver = resolver;
    }

    public CommandCatalog getCmdCatalog() {
        return cmdCatalog;
    }

    public CommandRegistry getCandidates() {
        return candidates;
    }

    public CommandRegistry getInferredCatalog() {
        return inferredCatalog;
    }

    public CommandSiteCatalog getHostCatalog() {
        return hostCatalog;
    }

    public CommandSiteCatalog getJvmCatalog() {
        return jvmCatalog;
    }

    public CommandSiteCatalog getUnionCatalog() {
        return unionCatalog;
    }

    public ExecSiteResolver getResolver() {
        return resolver;
    }

    public FinalPlacement rewrite(CmdOp cmdOp, ExecSite preferredExecSite) {
        Set<ExecSite> preferredExecSites = Set.of(preferredExecSite);
        FinalPlacement result = rewrite(cmdOp, preferredExecSites);
        return result;
    }

    public FinalPlacement rewrite(CmdOp cmdOp, Set<ExecSite> preferredExecSites) {
        // Try to resolve the command on a certain docker image.
        CmdOpVisitorCandidatePlacer commandPlacer = new CmdOpVisitorCandidatePlacer(cmdCatalog, candidates, inferredCatalog, resolver, preferredExecSites);
        PlacedCommand placedCommand = cmdOp.accept(commandPlacer);
        CandidatePlacement candidatePlacement = new CandidatePlacement(placedCommand, commandPlacer.getVarToPlacement());
        System.out.println("Candidate Placement: " + candidatePlacement);
        FinalPlacement placed = FinalPlacer.place(candidatePlacement);
        System.out.println("Placed: " + placed);
        FinalPlacement inlined = FinalPlacementInliner.inline(placed);
        return inlined;
    }

    public Process exec(ProcessRunner execCxt, FileMapper fileMapper, CmdOp cmdOp, ExecSite preferredExecSite) throws IOException {
        FinalPlacement finalPlacement = rewrite(cmdOp, preferredExecSite);
        ProcessBuilderFinalPlacement pb = new ProcessBuilderFinalPlacement(fileMapper, resolver, unionCatalog);
        pb.command(finalPlacement);
        Process p = pb.start(execCxt);
        return p;
    }

    public Process exec(ProcessRunner execCxt, FileMapper fileMapper, FinalPlacement placement) throws IOException {
        ProcessBuilderFinalPlacement pb = new ProcessBuilderFinalPlacement(fileMapper, resolver, unionCatalog);
        pb.command(placement);
        Process p = pb.start(execCxt);
        return p;
    }

    public record CmdArgActiveProcessSubstitution(CmdArg cmdArg, Deque<Process> processes) {}

    public CmdArgActiveProcessSubstitution exec(ProcessRunner execCxt, FileMapper fileMapper, CmdArg cmdArg, ExecSite preferredExecSite) {

        CmdArgActiveProcessSubstitution result;
        // XXX Visitor!
        if (cmdArg instanceof CmdArgCmdOp cmdArgOp) {
            // probeResultsCatalog = getInferredCatalog();
            CmdOp cmdOp = cmdArgOp.cmdOp();

            CmdOpVisitorCandidatePlacer commandPlacer = new CmdOpVisitorCandidatePlacer(cmdCatalog, candidates, inferredCatalog, resolver, Set.of(preferredExecSite));
            PlacedCommand placedCommand = cmdOp.accept(commandPlacer);
            CandidatePlacement candidatePlacement = new CandidatePlacement(placedCommand, commandPlacer.getVarToPlacement());
            System.out.println("Candidate Placement: " + candidatePlacement);

            FinalPlacement placed = FinalPlacer.place(candidatePlacement);
            // System.out.println("Placed: " + placed);

            FinalPlacement inlined = FinalPlacementInliner.inline(placed);
            System.out.println("Inlined final placement: " + inlined);

    //      PlacedCmd placedCmd = resolvedInlined.cmdOp();
            PlacedCmd placedCmd = inlined.cmdOp();
            // CmdArg cmdArg = CmdArg.ofProcessSubstution(placedCmd.cmdOp());
            ExecSite topLevelExecSite = placedCmd.execSite();
            // CmdArg cmdArg = CmdArg.ofProcessSubstution(cmdOp);
                    // virtual-to-physical command rewrite using FinalPlacementResolver looses the arg-parser-shim.

            //        FinalPlacement resolvedInlined = FinalPlacementResolver.resolve(inlined, resolver, inferredCatalog);
            //        System.out.println("Resolved inlined final placement: " + resolvedInlined);
            ProcessBuilderFinalPlacement pb = new ProcessBuilderFinalPlacement(fileMapper, resolver, unionCatalog);
            ExecSiteToProcessDispatcher dispatcher = pb.newDispatcher(execCxt);
            CmdOpVisitorToBase visitor = topLevelExecSite.accept(dispatcher);
            // Issue: ArgTransformer does not call CmdOpVisitorToBase#toProcessBuilder and can't use the resolution there
            // Possible fix: Add CmdOpVisitorToBase#resolve method to make resolution accessible
            //   (or is this part better handled on the common dispatcher level?
            //    Well, CmdOpVisitorToBase is bound to a specific execSite, whereas the dispatcher isn't!)
            CmdArgVisitor<CmdArg> argTransformer = visitor.getCmdArgTransformer();
            CmdArg rewrittenArg = cmdArg.accept(argTransformer);
            Deque<Process> processes = dispatcher.getProcesses();
            result = new CmdArgActiveProcessSubstitution(rewrittenArg, processes);
        } else {
            result = new CmdArgActiveProcessSubstitution(cmdArg, new ArrayDeque<>());
        }
        return result;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        protected Boolean allowLocateOnHost;

        public Builder setAllowLocateOnHost(Boolean allowLocateOnHost) {
            this.allowLocateOnHost = allowLocateOnHost;
            return this;
        }

        public CmdExecSystem build() {
            JvmCommandRegistry jvmCmdRegistry = InitCommandRegistry.initJvmCmdRegistry(new JvmCommandRegistry());
            CommandRegistry candidates = InitCommandRegistry.initCmdCandRegistry(new CommandRegistry());

            CommandRegistry inferredCatalog = new CommandRegistry();

            CommandCatalog cmdCatalog = new CommandCatalogImpl();

            CommandLocator hostCommandLocator = Boolean.FALSE.equals(allowLocateOnHost)
                ? CommandLocatorNull.get()
                : new CommandLocatorHost();

            CommandSiteCatalog hostCatalog = new CommandCatalogOverLocator(ExecSiteCurrentHost.get(), hostCommandLocator);
            CommandSiteCatalog jvmCatalog = new CommandCatalogOverLocator(ExecSites.jvm(), new CommandLocatorJvmRegistry(jvmCmdRegistry));
            CommandSiteCatalog unionCatalog = new CommandCatalogUnion(List.of(candidates, hostCatalog, jvmCatalog, inferredCatalog));

            ExecSiteProbeResults probeResults = new ExecSiteProbeResults();
            // TODO Have image introspector write into cmdAvailability without having to know about exec sites.
            // Need an adapter or cmdAvailability.asDockerImageMap().

            // Model shellModel = RDFDataMgr.loadModel("shell-ontology.ttl");
            ImageIntrospector imageIntrospector = ImageIntrospectorImpl.of(); // shellModel, probeResults);
            // imageIntrospector = new ImageIntrospectorCaching(imageIntrospector);

            ExecSiteResolver resolver = new ExecSiteResolver(candidates, jvmCmdRegistry, probeResults, imageIntrospector);

            return new CmdExecSystem(cmdCatalog, jvmCmdRegistry, candidates, inferredCatalog,
                 hostCatalog, jvmCatalog, unionCatalog,
                 probeResults, imageIntrospector, resolver);
        }
    }
}
