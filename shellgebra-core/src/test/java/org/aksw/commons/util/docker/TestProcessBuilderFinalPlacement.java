package org.aksw.commons.util.docker;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.arg.CmdPrefix;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.aksw.shellgebra.exec.graph.ProcessRunnerPosix;
import org.aksw.shellgebra.exec.model.ExecSite;
import org.aksw.shellgebra.exec.model.ExecSites;
import org.aksw.shellgebra.shim.core.ArgumentList;
import org.aksw.vshell.registry.CmdExecSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestProcessBuilderFinalPlacement {
    private static final Logger logger = LoggerFactory.getLogger(TestProcessBuilderFinalPlacement.class);

    // @Test
    public void test01() throws IOException, Exception {
        ContainerUtils.setGlobalRetryCountIfAbsent(1);

        CmdExecSystem execSystem = CmdExecSystem.newBuilder().build();

        // Some command expression.
        // "echo 'test' | lbzip2 -c | bzip2 -cd | cat - <(echo done)"
        System.out.println(execSystem.getResolver().resolve("/virt/lbzip2"));
        CmdOp cmdOp = createCmdOp();

        // FinalPlacement inlined = FinalPlacementInliner.inline(placed);

        // TODO Resolution at this point is bad because we lose the original command
        // and the original command parser.
        // FinalPlacement resolvedInlined = FinalPlacementResolver.resolve(inlined, resolver, unionCatalog);
        // Resolve commands w.r.t. the final placement.
        // System.out.println("Inlined: " + resolvedInlined);
        // CommandParserCatalog parserCatalog = new CommandParserCatalogImpl(unionCatalog, jvmCmdRegistry);

        FileMapper fileMapper = FileMapper.of("/tmp/shared");
        try (ProcessRunner context = ProcessRunnerPosix.create()) {
            context.setOutputLineReaderUtf8(line -> logger.info("Got line: " + line));
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
            // InitCommandRegistry.initJvmCmdRegistry(context.getJvmCmdRegistry());
            // InitCommandRegistry.initJvmCmdRegistry(execSystem.getJvmCmdRegistry());
            // Try to resolve the command on a certain docker image.
            ExecSite qleverExecSite = ExecSites.docker("adfreiburg/qlever:commit-a307781");
            Process p = execSystem.exec(context, fileMapper, cmdOp, qleverExecSite);
//
//            ProcessBuilderFinalPlacement pb = new ProcessBuilderFinalPlacement(fileMapper, execSystem.getResolver(), execSystem.getUnionCatalog());
//            pb.command(inlined);
//            Process p = pb.start(context);

            // Thread.sleep(5000);

            System.out.println("Shutting context down.");
            p.waitFor();
            context.shutdown();
        }
    }

    private static CmdOp createCmdOp() {
        CmdOp cmdOp;
        // TODO Make this test case work reliably!
        if (true) {
            CmdOpExec cmdOp1 = CmdOpExec.ofLiterals("/virt/lbzip2", "-c");
            CmdOp cmdOp2 = CmdOpGroup.of(
                // FIXME Adding this line causes an NPE!!!
                // CmdOpExec.ofLiterals("/virt/echo", "FOOBAR"),
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
        }
        return cmdOp;
    }
}
