package org.aksw.vshell.registry;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.aksw.commons.util.docker.ImageIntrospectorImpl;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.aksw.shellgebra.exec.model.ExecSite;
import org.aksw.shellgebra.exec.model.ExecSiteCurrentHost;
import org.aksw.shellgebra.exec.model.ExecSites;
import org.aksw.shellgebra.exec.model.PlacedCommand;
import org.aksw.shellgebra.model.osreo.ImageIntrospector;
import org.aksw.shellgebra.registry.init.InitCommandRegistry;

/**
 * The default command rewrite system.
 * Technically a facade over command catalogs for different execution sites (host, docker, jvm).
 *
 * TODO This class currently is only command placement but not execution. This facade should also cover the execution aspect.
 */
public class CmdExecSystem {
    private JvmCommandRegistry jvmCmdRegistry;
    private CommandRegistry candidates;

    private CommandRegistry inferredCatalog;
    private CommandCatalog hostCatalog;
    private CommandCatalog jvmCatalog;
    private CommandCatalog unionCatalog;

    private ExecSiteProbeResults probeResults;
    // TODO Have image introspector write into cmdAvailability without having to know about exec sites.
    // Need an adapter or cmdAvailability.asDockerImageMap().

    // Model shellModel = RDFDataMgr.loadModel("shell-ontology.ttl");
    private ImageIntrospector imageIntrospector; // shellModel, probeResults);
    // imageIntrospector = new ImageIntrospectorCaching(imageIntrospector);

    private ExecSiteResolver resolver;

    /** Use {@link #newBuilder()} to create instances. */
    CmdExecSystem() {
        super();
        init();
    }

    public void init() {
        jvmCmdRegistry = InitCommandRegistry.initJvmCmdRegistry(new JvmCommandRegistry());
        candidates = InitCommandRegistry.initCmdCandRegistry(new CommandRegistry());

        inferredCatalog = new CommandRegistry();
        hostCatalog = new CommandCatalogOverLocator(ExecSiteCurrentHost.get(), new CommandLocatorHost());
        jvmCatalog = new CommandCatalogOverLocator(ExecSites.jvm(), new CommandLocatorJvmRegistry(jvmCmdRegistry));
        unionCatalog = new CommandCatalogUnion(List.of(candidates, hostCatalog, jvmCatalog, inferredCatalog));

        probeResults = new ExecSiteProbeResults();
        // TODO Have image introspector write into cmdAvailability without having to know about exec sites.
        // Need an adapter or cmdAvailability.asDockerImageMap().

        // Model shellModel = RDFDataMgr.loadModel("shell-ontology.ttl");
        imageIntrospector = ImageIntrospectorImpl.of(); // shellModel, probeResults);
        // imageIntrospector = new ImageIntrospectorCaching(imageIntrospector);

        resolver = new ExecSiteResolver(candidates, jvmCmdRegistry, probeResults, imageIntrospector);
    }

    public JvmCommandRegistry getJvmCmdRegistry() {
        return jvmCmdRegistry;
    }

    public CommandRegistry getCandidates() {
        return candidates;
    }

    public CommandRegistry getInferredCatalog() {
        return inferredCatalog;
    }

    public CommandCatalog getHostCatalog() {
        return hostCatalog;
    }

    public CommandCatalog getJvmCatalog() {
        return jvmCatalog;
    }

    public CommandCatalog getUnionCatalog() {
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
        CmdOpVisitorCandidatePlacer commandPlacer = new CmdOpVisitorCandidatePlacer(candidates, inferredCatalog, resolver, preferredExecSites);
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

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
// TODO Make relevant aspects configurable.
//        private JvmCommandRegistry jvmCmdRegistry;
//        private CommandRegistry candidates;
//
//        private CommandRegistry inferredCatalog;
//        private CommandCatalog hostCatalog;
//        private CommandCatalog jvmCatalog;
//        private CommandCatalog unionCatalog;
//
//        private ExecSiteProbeResults probeResults;
//        // TODO Have image introspector write into cmdAvailability without having to know about exec sites.
//        // Need an adapter or cmdAvailability.asDockerImageMap().
//
//        // Model shellModel = RDFDataMgr.loadModel("shell-ontology.ttl");
//        private ImageIntrospector imageIntrospector; // shellModel, probeResults);
//        // imageIntrospector = new ImageIntrospectorCaching(imageIntrospector);
//
//        private ExecSiteResolver resolver;


        public CmdExecSystem build() {
            return new CmdExecSystem();
        }
    }
}
