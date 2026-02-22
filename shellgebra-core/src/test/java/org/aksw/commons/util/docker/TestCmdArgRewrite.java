package org.aksw.commons.util.docker;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Deque;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgWord;
import org.aksw.shellgebra.algebra.cmd.arg.CmdPrefix;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenPath;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.exec.graph.ProcessIoWrapper;
import org.aksw.shellgebra.exec.graph.ProcessRunnerPosix;
import org.aksw.shellgebra.exec.model.ExecSite;
import org.aksw.shellgebra.exec.model.ExecSites;
import org.aksw.shellgebra.registry.init.InitCommandRegistry;
import org.aksw.shellgebra.shim.core.ArgumentList;
import org.aksw.vshell.registry.CmdExecSystem;
import org.aksw.vshell.registry.CmdExecSystem.CmdArgActiveProcessSubstitution;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCmdArgRewrite {
    private static final Logger logger = LoggerFactory.getLogger(TestCmdArgRewrite.class);

    @Test
    public void test01() throws IOException, Exception {
        ContainerUtils.setGlobalRetryCountIfAbsent(1);
        CmdExecSystem cmdExecSystem = CmdExecSystem.newBuilder().build();

        InitCommandRegistry.initJvmCmdRegistry(cmdExecSystem.getJvmCmdRegistry());
        InitCommandRegistry.initCmdCandRegistry(cmdExecSystem.getCandidates());

        CmdOp cmdOp = createCmdOp();
        System.out.println("CmdOp: " + cmdOp);

        // Try to resolve the command on a certain docker image.
        ExecSite qleverExecSite = ExecSites.docker("adfreiburg/qlever:commit-a307781");
        CmdArg cmdArg = CmdArg.ofProcessSubstution(cmdOp);

        FileMapper fileMapper = FileMapper.of("/tmp/shared");
        try (ProcessIoWrapper wrapper = ProcessRunnerPosix.create()) {
            context.setOutputLineReaderUtf8(line -> logger.info("Got line: " + line));
            context.setErrorLineReaderUtf8(logger::info);
            context.setInputPrintStreamUtf8(out -> {
                out.println("hello world");
            });

            CmdArgActiveProcessSubstitution cmdArgActive = cmdExecSystem.exec(context, fileMapper, cmdArg, qleverExecSite);
            CmdArg rewrittenArg = cmdArgActive.cmdArg();
            Deque<Process> processes = cmdArgActive.processes();

            System.out.println("Processes: " + processes.size());
            System.out.println("Rewritten arg: " + rewrittenArg);

            TokenPath token = (TokenPath)((CmdArgWord)rewrittenArg).tokens().get(0);
            try (InputStream in = Files.newInputStream(Path.of(token.path()))) {
                System.out.println("Content: " + IOUtils.toString(in, StandardCharsets.UTF_8));
            }
        }
    }

    public CmdOp createCmdOpX() {
        // return CmdOpExec.ofLiterals("/virt/echo", "done.");
        return CmdOpGroup.of(
                CmdOpExec.ofLiterals("/virt/echo", "line 1/2 done."),
                CmdOpExec.ofLiterals("/virt/echo", "line 2/2 done."));
    }

    public CmdOp createCmdOp() {
        // Some command expression.
        // "echo 'test' | lbzip2 -c | bzip2 -cd | cat - <(echo done)"
        // System.out.println(resolver.resolve("/virt/lbzip2"));
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
            // ISSUE If in a pipeline there is a group then we MUST use anon pipes!
            CmdOpExec cmdOp1 = CmdOpExec.ofLiterals("/virt/lbzip2", "-c");
            CmdOp cmdOp2;

            if (false) {
                cmdOp2 = CmdOpGroup.of(
                    CmdOpExec.ofLiterals("/virt/bzip2", "-dc"),
                    CmdOpExec.ofLiterals("/virt/echo", "done.")
                );
            } else {
                // TODO Do not use CmdOpExec.ofLiterals
                // Instead: use a command registry with shim-parsers so that arguments are validated.
//                cmdOp2 = CmdOpExec.ofLiterals("/virt/bzip2", "-dc");
                cmdOp2 = CmdOpExec.ofLiterals("/virt/echo", "done.");
            }

            cmdOp = CmdOpPipeline.of(cmdOp1, cmdOp2);
            // cmdOp = cmdOp1;
            // cmdOp = cmdOp2;
        }
        return cmdOp;
    }
}

//
//        // TODO Issue: the placed command my use the unresolved path (e.g. /virt/bzip2) instead of the resolved one (e.g. /usr/bin/lbzip2).
//        // So either (1) the command has to be resolved for each exec site (well, i think the virtual command is fine at this point)
//        // or (2) we need a final rewrite that replaces the virtual command with the physical one for the finally chosen exec site.
//        // I guess the proper solution is (2).
//
//        FinalPlacement finalPlacement = cmdExecSystem.rewrite(cmdOp, qleverExecSite);
//        PlacedCmd placedCmd = finalPlacement.cmdOp();
//        // PlacedCmd placedCmd = inlined.cmdOp();
//        ExecSite topLevelExecSite = placedCmd.execSite();
// FIXME Reuse existing jvmCmdRegistry!
// InitCommandRegistry.initJvmCmdRegistry(context.getJvmCmdRegistry());
//InitCommandRegistry.initJvmCmdRegistry(cmdExecSystem.getJvmCmdRegistry());
//ProcessBuilderFinalPlacement pb = new ProcessBuilderFinalPlacement(fileMapper, resolver, unionCatalog);
//ExecSiteToProcessDispatcher dispatcher = pb.newDispatcher(context);
//CmdOpVisitorToBase visitor = topLevelExecSite.accept(dispatcher);
//// Issue: ArgTransformer does not call CmdOpVisitorToBase#toProcessBuilder and can't use the resolution there
//// Possible fix: Add CmdOpVisitorToBase#resolve method to make resolution accessible
////   (or is this part better handled on the common dispatcher level?
////    Well, CmdOpVisitorToBase is bound to a specific execSite, whereas the dispatcher isn't!)
//CmdArgVisitor<CmdArg> argTransformer = visitor.getCmdArgTransformer();
//CmdArg rewrittenArg = cmdArg.accept(argTransformer);
//Deque<Process> processes = dispatcher.getProcesses();

// CmdOpVisitorCandidatePlacer commandPlacer = new CmdOpVisitorCandidatePlacer(candidates, probeResultsCatalog, resolver, Set.of(qleverExecSite));
// PlacedCommand placedCommand = cmdOp.accept(commandPlacer);
// CandidatePlacement candidatePlacement = new CandidatePlacement(placedCommand, commandPlacer.getVarToPlacement());
// System.out.println("Candidate Placement: " + candidatePlacement);

// FinalPlacement placed = FinalPlacer.place(candidatePlacement);
// System.out.println("Placed: " + placed);

// FinalPlacement inlined = FinalPlacementInliner.inline(placed);
// System.out.println("Inlined final placement: " + inlined);

// virtual-to-physical command rewrite using FinalPlacementResolver looses the arg-parser-shim.

//FinalPlacement resolvedInlined = FinalPlacementResolver.resolve(inlined, resolver, inferredCatalog);
//System.out.println("Resolved inlined final placement: " + resolvedInlined);

//  @Test
//  public void testCmdArgRewrite01() throws IOException, Exception {
//      FileMapper fileMapper = FileMapper.of("/tmp/shared");
//      try (ProcessRunner runner = ProcessRunnerPosix.create()) {
//          runner.setOutputLineReaderUtf8(logger::info);
//          runner.setErrorLineReaderUtf8(logger::info);
//          runner.setInputPrintStreamUtf8(out -> {
//              logger.info("Data generation thread started.");
//              for (int i = 0; i < 10000; ++i) {
//                  out.println("" + i);
//              }
//              out.flush();
//              logger.info("Data generation thread terminated.");
//          });
//
//
//
//          System.out.println("Process 1");
//          ProcessBuilderNative.of("head", "-n 2").start(runner).waitFor();
//          Thread.sleep(1000);
//
//          System.out.println("Process 2");
//          ProcessBuilderNative.of("head", "-n 4").start(runner).waitFor();
//
//          TestCommandRegistry.initJvmCmdRegistry(runner.getJvmCmdRegistry());
//
//          System.out.println("Process 6");
//          ProcessBuilderPipeline.of(
//              ProcessBuilderJvm.of("/bin/head", "-n10"),
//              ProcessBuilderDocker.of("/usr/bin/lbzip2", "-c")
//                  .interactive(true)
//                  .imageRef("nestio/lbzip2").fileMapper(fileMapper),
//              ProcessBuilderJvm.of("/jvm/bzip2", "-d"))
//              .start(runner)
//              .waitFor();
//
//          System.out.println("All processes completed.");
//      }
//  }
//  logger.info("Data generation thread started.");
//  for (int i = 0; i < 10000; ++i) {
//      out.println("" + i);
//  }
//  out.flush();
//  logger.info("Data generation thread terminated.");
//visitor.getDispatcher();
//
//    pb.command(inlined);
//    Process p = pb.start(context);
//
//    // Thread.sleep(5000);
//
//    System.out.println("Shutting context down.");
//    p.waitFor();
//    context.shutdown();
//  // This is the catalog of virtual-command to implementation - its both parser and executer.
//  JvmCommandRegistry jvmCmdRegistry = InitCommandRegistry.initJvmCmdRegistry(new JvmCommandRegistry());
//  CommandRegistry candidates = InitCommandRegistry.initCmdCandRegistry(new CommandRegistry());
//
//  CommandRegistry probeResultsCatalog = new CommandRegistry();
//  CommandCatalog hostCatalog = new CommandCatalogOverLocator(ExecSiteCurrentHost.get(), new CommandLocatorHost());
//  CommandCatalog jvmCatalog = new CommandCatalogOverLocator(ExecSites.jvm(), new CommandLocatorJvmRegistry(jvmCmdRegistry));
//  CommandCatalog unionCatalog = new CommandCatalogUnion(List.of(candidates, hostCatalog, jvmCatalog, probeResultsCatalog));
//
//  ExecSiteProbeResults probeResults = new ExecSiteProbeResults();
//  // TODO Have image introspector write into cmdAvailability without having to know about exec sites.
//  // Need an adapter or cmdAvailability.asDockerImageMap().
//
//  // Model shellModel = RDFDataMgr.loadModel("shell-ontology.ttl");
//  ImageIntrospector imageIntrospector = ImageIntrospectorImpl.of(); // shellModel, probeResults);
//  // imageIntrospector = new ImageIntrospectorCaching(imageIntrospector);
//
//  ExecSiteResolver resolver = new ExecSiteResolver(candidates, jvmCmdRegistry, probeResults, imageIntrospector);

