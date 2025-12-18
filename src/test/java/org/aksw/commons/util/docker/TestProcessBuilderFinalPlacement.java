package org.aksw.commons.util.docker;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.cmd.op.prefix.CmdPrefix;
import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.aksw.shellgebra.exec.graph.ProcessRunnerPosix;
import org.aksw.shellgebra.exec.model.ExecSite;
import org.aksw.shellgebra.exec.model.ExecSiteCurrentHost;
import org.aksw.shellgebra.exec.model.ExecSites;
import org.aksw.shellgebra.exec.model.PlacedCommand;
import org.aksw.vshell.registry.CandidatePlacement;
import org.aksw.vshell.registry.CmdOpVisitorCandidatePlacer;
import org.aksw.vshell.registry.CommandCatalog;
import org.aksw.vshell.registry.CommandCatalogOverLocator;
import org.aksw.vshell.registry.CommandCatalogUnion;
import org.aksw.vshell.registry.CommandLocatorHost;
import org.aksw.vshell.registry.CommandLocatorJvmRegistry;
import org.aksw.vshell.registry.CommandRegistry;
import org.aksw.vshell.registry.ExecSiteProbeResults;
import org.aksw.vshell.registry.ExecSiteResolver;
import org.aksw.vshell.registry.FinalPlacement;
import org.aksw.vshell.registry.FinalPlacementInliner;
import org.aksw.vshell.registry.FinalPlacer;
import org.aksw.vshell.registry.JvmCommandRegistry;
import org.aksw.vshell.registry.ProcessBuilderFinalPlacement;
import org.aksw.vshell.shim.rdfconvert.ArgumentList;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestProcessBuilderFinalPlacement {
    private static final Logger logger = LoggerFactory.getLogger(TestProcessBuilderFinalPlacement.class);

    @Test
    public void test01() throws IOException, Exception {
        ContainerUtils.setGlobalRetryCountIfAbsent(1);

        JvmCommandRegistry jvmCmdRegistry = TestCommandRegistry.initJvmCmdRegistry(new JvmCommandRegistry());
        CommandRegistry candidates = TestCommandRegistry.initCmdCandRegistry(new CommandRegistry());

        CommandRegistry inferredCatalog = new CommandRegistry();
        CommandCatalog hostCatalog = new CommandCatalogOverLocator(ExecSiteCurrentHost.get(), new CommandLocatorHost());
        CommandCatalog jvmCatalog = new CommandCatalogOverLocator(ExecSites.jvm(), new CommandLocatorJvmRegistry(jvmCmdRegistry));
        CommandCatalog unionCatalog = new CommandCatalogUnion(List.of(candidates, hostCatalog, jvmCatalog, inferredCatalog));

        ExecSiteProbeResults probeResults = new ExecSiteProbeResults();
        // TODO Have image introspector write into cmdAvailability without having to know about exec sites.
        // Need an adapter or cmdAvailability.asDockerImageMap().

        Model shellModel = RDFDataMgr.loadModel("shell-ontology.ttl");
        ImageIntrospector imageIntrospector = ImageIntrospectorImpl.of(shellModel, probeResults);
        imageIntrospector = new ImageIntrospectorCaching(imageIntrospector);

        ExecSiteResolver resolver = new ExecSiteResolver(candidates, jvmCmdRegistry, probeResults, imageIntrospector);

        // Some command expression.
        // "echo 'test' | lbzip2 -c | bzip2 -cd | cat - <(echo done)"
        System.out.println(resolver.resolve("/virt/lbzip2"));
        CmdOp cmdOp;
        if (false) {
            CmdOpExec cmdOp1 = CmdOpExec.ofLiterals("/virt/lbzip2", "-c");
            CmdOp cmdOp2 = CmdOpGroup.of(
                CmdOpExec.ofLiterals("/virt/bzip2", "-dc"),
                new CmdOpExec(List.<CmdPrefix>of(), "/virt/cat", ArgumentList.of(
                    CmdArg.ofLiteral("-"),
                    CmdArg.ofProcessSubstution(CmdOpExec.ofLiterals("/virt/echo", "done."))))
            );
            // TODO Do not use CmdOpExec.ofLiterals
            // Instead: use a command registry with shim-parsers so that arguments are validated.

            cmdOp = CmdOpPipeline.of(cmdOp1, cmdOp2);
        } else {
            CmdOpExec cmdOp1 = CmdOpExec.ofLiterals("/virt/lbzip2", "-c");
            CmdOp cmdOp2 = CmdOpGroup.of(
                CmdOpExec.ofLiterals("/virt/bzip2", "-dc"),
                CmdOpExec.ofLiterals("/virt/echo", "done.")
            );
            // TODO Do not use CmdOpExec.ofLiterals
            // Instead: use a command registry with shim-parsers so that arguments are validated.

            cmdOp = CmdOpPipeline.of(cmdOp1, cmdOp2);
        }

        // Try to resolve the command on a certain docker image.
        ExecSite qleverExecSite = ExecSites.docker("adfreiburg/qlever:commit-a307781");

        CmdOpVisitorCandidatePlacer commandPlacer = new CmdOpVisitorCandidatePlacer(candidates, inferredCatalog, resolver, Set.of(qleverExecSite));
        PlacedCommand placedCommand = cmdOp.accept(commandPlacer);
        CandidatePlacement candidatePlacement = new CandidatePlacement(placedCommand, commandPlacer.getVarToPlacement());
        System.out.println("Candidate Placement: " + candidatePlacement);

        FinalPlacement placed = FinalPlacer.place(candidatePlacement);
        System.out.println("Placed: " + placed);

        FinalPlacement inlined = FinalPlacementInliner.inline(placed);

        // TODO Resolution at this point is bad because we lose the original command
        // and the original command parser.
        // FinalPlacement resolvedInlined = FinalPlacementResolver.resolve(inlined, resolver, unionCatalog);
        // Resolve commands w.r.t. the final placement.
        // System.out.println("Inlined: " + resolvedInlined);
        // CommandParserCatalog parserCatalog = new CommandParserCatalogImpl(unionCatalog, jvmCmdRegistry);

        FileMapper fileMapper = FileMapper.of("/tmp/shared");
        try (ProcessRunner context = ProcessRunnerPosix.create()) {
            context.setOutputLineReaderUtf8(logger::info);
            context.setErrorLineReaderUtf8(logger::info);
            context.setInputPrintStreamUtf8(out -> {
                out.println("hello world");
//                logger.info("Data generation thread started.");
//                for (int i = 0; i < 10000; ++i) {
//                    out.println("" + i);
//                }
//                out.flush();
//                logger.info("Data generation thread terminated.");
            });

            // FIXME Reuse existing jvmCmdRegistry!
            TestCommandRegistry.initJvmCmdRegistry(context.getJvmCmdRegistry());
            ProcessBuilderFinalPlacement pb = new ProcessBuilderFinalPlacement(fileMapper, resolver, unionCatalog);
            pb.command(inlined);
            Process p = pb.start(context);
            p.waitFor();
        }
    }
}
