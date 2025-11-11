package org.aksw.commons.util.docker;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.exec.IProcessBuilder;
import org.aksw.shellgebra.exec.Stage;
import org.aksw.shellgebra.exec.SysRuntimeCore;
import org.aksw.shellgebra.exec.SysRuntimeCoreExecSiteFactory;
import org.aksw.shellgebra.exec.SysRuntimeCoreExecSiteFactoryPool;
import org.aksw.shellgebra.exec.SysRuntimeFactoryDocker;
import org.aksw.shellgebra.exec.model.ExecSite;
import org.aksw.shellgebra.exec.model.ExecSites;
import org.aksw.shellgebra.exec.model.PlacedCommand;
import org.aksw.shellgebra.exec.shell.ShellEnv;
import org.aksw.vshell.registry.CandidatePlacement;
import org.aksw.vshell.registry.CmdOpVisitorCandidatePlacer;
import org.aksw.vshell.registry.CommandAvailability;
import org.aksw.vshell.registry.CommandRegistry;
import org.aksw.vshell.registry.ExecSiteResolver;
import org.aksw.vshell.registry.FinalPlacement;
import org.aksw.vshell.registry.FinalPlacementInliner;
import org.aksw.vshell.registry.FinalPlacer;
import org.aksw.vshell.registry.JvmCmdTest;
import org.aksw.vshell.registry.JvmCommand;
import org.aksw.vshell.registry.JvmCommandExecutor;
import org.aksw.vshell.registry.JvmCommandExecutorImpl;
import org.aksw.vshell.registry.JvmCommandRegistry;
import org.aksw.vshell.registry.JvmCommandWhich;
import org.aksw.vshell.registry.JvmContext;
import org.aksw.vshell.registry.PlacedCmdOpToStage;
import org.aksw.vshell.shim.rdfconvert.JvmCommandTranscode;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Test;

import junit.framework.Assert;

public class TestCommandRegistry {
    @Test
    public void test01() throws IOException {
        String testcontainers_retryCount = System.getProperty("testcontainers.retryCount");
        if (testcontainers_retryCount == null) {
            System.setProperty("testcontainers.retryCount", "1");
        }

        JvmCommandRegistry jvmCmdRegistry = initJvmCmdRegistry(new JvmCommandRegistry());
        CommandRegistry candidates = initCmdCandRegistry(new CommandRegistry());

        CommandAvailability cmdAvailability = new CommandAvailability();
        // TODO Have image introspector write into cmdAvailability without having to know about exec sites.
        // Need an adapter or cmdAvailability.asDockerImageMap().

        Model shellModel = RDFDataMgr.loadModel("shell-ontology.ttl");
        ImageIntrospector imageIntrospector = ImageIntrospectorImpl.of(shellModel, cmdAvailability);
        imageIntrospector = new ImageIntrospectorCaching(imageIntrospector);

        ExecSiteResolver resolver = new ExecSiteResolver(candidates, jvmCmdRegistry,
            cmdAvailability, imageIntrospector);

        // Some command expression.
        System.out.println(resolver.resolve("/virt/lbzip2"));
        CmdOpExec cmdOp1 = CmdOpExec.ofLiterals("/virt/lbzip2", "-d");
        CmdOp cmdOp2 = CmdOpGroup.of(
            CmdOpExec.ofLiterals("/virt/bzip2", "-d"),
            CmdOpExec.ofLiterals("/usr/bin/echo", "done.")
        );
        // TODO Do not use CmdOpExec.ofLiterals
        // Instead: use a command registry with shim-parsers so that arguments are validated.

        CmdOp cmdOp = CmdOpPipeline.of(cmdOp1, cmdOp2);

        // Try to resolve the command on a certain docker image.
        ExecSite qleverExecSite = ExecSites.docker("adfreiburg/qlever:commit-a307781");

        CmdOpVisitorCandidatePlacer commandPlacer = new CmdOpVisitorCandidatePlacer(candidates, resolver, Set.of(qleverExecSite));
        PlacedCommand placedCommand = cmdOp.accept(commandPlacer);
        CandidatePlacement candidatePlacement = new CandidatePlacement(placedCommand, commandPlacer.getVarToPlacement());

        FinalPlacement placed = FinalPlacer.place(candidatePlacement);
        System.out.println("Placed: " + placed);

        FinalPlacement inlined = FinalPlacementInliner.inline(placed);

        System.out.println("Inlined: " + inlined);
//        if (true) {
//            return;
//        }

        // pb.redirectError(Redirect.)
        // Final step: convert to stage (or bound stage?)
        FileMapper fileMapper = FileMapper.of("/tmp/shared");
        // TODO PlacedCmdOpToStage should probably accept a resolver as argument!
        ShellEnv shellEnv = new ShellEnv(); // Perhaps pass a shellEnv for book keeping of streams / file writers?

        Stage stage = PlacedCmdOpToStage.of(fileMapper, resolver).toStage(inlined);
        String str = stage.fromNull().toByteSource().asCharSource(StandardCharsets.UTF_8).read();
        System.out.println(str);
        System.out.println(placedCommand);

        // Issue: For the qlever use case, we don't want a stage.
        // instead we want: the expression, with allocated file names and file writer tasks.

        // System.out.println(resolver.resolve("/usr/bin/lbzip2"));
        // CommandRegistry hostRegistry = new CommandRegistryOverLocator(ExecSiteCurrentHost.get(), new CommandLocatorHost());
        // CommandRegistry jvmRegistry = new CommandRegistryOverLocator(ExecSites.jvm(), new CommandLocatorJvmRegistry(jvmCmdRegistry));
        // CommandRegistry baseRegistry = new CommandRegistryUnion(List.of(jvmRegistry, candidates, hostRegistry));
    }

    @Test
    public void test02() throws IOException, InterruptedException {
        JvmCommandRegistry jvmCmdRegistry = initJvmCmdRegistry(new JvmCommandRegistry());
        SysRuntimeFactoryDocker dockerFactory = SysRuntimeFactoryDocker.create();

        String expectedStr = "Hello world";
        String actualStr;
        try (SysRuntimeCoreExecSiteFactory f = new SysRuntimeCoreExecSiteFactoryPool(jvmCmdRegistry, dockerFactory)) {
            try (SysRuntimeCore r = f.getRuntime(ExecSites.docker("nestio/lbzip2"))) {
                actualStr = r.execCmd("echo", expectedStr);
            }
        }
        Assert.assertEquals(expectedStr,actualStr);
    }

    @Test
    public void testJvmWhichDirect() throws IOException {
        JvmCommandRegistry jvmCmdRegistry = initJvmCmdRegistry(new JvmCommandRegistry());
        JvmContext context = new JvmContext(jvmCmdRegistry);
        context.getEnvironment().put("PATH", "/jvm:/bin");
        JvmCommandExecutor executor = new JvmCommandExecutorImpl(context);

        executor.run("which", "bzip2");

        String expectedStr = "/jvm/bzip2";
        String actualStr = executor.exec("which", "bzip2");
        Assert.assertEquals(expectedStr, actualStr);


        int actualValue = executor.run("test", "-e", actualStr);
        Assert.assertEquals(0, actualValue);
    }

    @Test
    public void testJvmWhichProcess() throws IOException {
        JvmCommandRegistry jvmCmdRegistry = initJvmCmdRegistry(new JvmCommandRegistry());
        JvmContext context = new JvmContext(jvmCmdRegistry);
        context.getEnvironment().put("PATH", "/jvm:/bin");
        JvmCommandExecutor executor = new JvmCommandExecutorImpl(context);

        IProcessBuilder<?> builder = executor.newProcessBuilder("which", "bzip2");
        // SystemUtils

//        String expectedStr = "/jvm/bzip2";
//        String actualStr = executor.exec("which", "bzip2");
        // Assert.assertEquals(expectedStr, actualStr);
    }

    public static JvmCommandRegistry initJvmCmdRegistry(JvmCommandRegistry jvmCmdRegistry) {
        // Core command: which - resolve short name to fully qualified command name.
        jvmCmdRegistry.put("/bin/which", new JvmCommandWhich());

        // Core command: test - return 0 if name is fully qualified command name - 1 otherwise.
        jvmCmdRegistry.put("/bin/test", new JvmCmdTest());

        CompressorStreamFactory csf = new CompressorStreamFactory();

        JvmCommand bzip2Cmd = JvmCommandTranscode.of(csf, CompressorStreamFactory.BZIP2);
        jvmCmdRegistry.put("/jvm/bzip2", bzip2Cmd);
        return jvmCmdRegistry;
    }

    public static CommandRegistry initCmdCandRegistry(CommandRegistry registry) {
        registry.put("/virt/lbzip2", ExecSites.docker("nestio/lbzip2"), "/usr/bin/lbzip2");

        // Note: There can be multiple candidates per exec site.
        registry.put("/virt/lbzip2", ExecSites.host(), "/usr/bin/lbzip2");
        registry.put("/virt/lbzip2", ExecSites.jvm(), "/jvm/bzip2");

        registry.put("/virt/bzip2", ExecSites.jvm(), "/jvm/bzip2");

        registry.put("/usr/bin/echo", ExecSites.host(), "/usr/bin/echo");
        return registry;
    }
}
