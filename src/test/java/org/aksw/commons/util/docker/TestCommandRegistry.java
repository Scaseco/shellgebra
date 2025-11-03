package org.aksw.commons.util.docker;

import java.util.Set;

import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.exec.model.ExecSite;
import org.aksw.shellgebra.exec.model.ExecSites;
import org.aksw.shellgebra.exec.model.PlacedCommand;
import org.aksw.vshell.registry.CommandAvailability;
import org.aksw.vshell.registry.CommandPlacer;
import org.aksw.vshell.registry.CommandRegistryImpl;
import org.aksw.vshell.registry.ExecSiteResolver;
import org.aksw.vshell.registry.JvmCommand;
import org.aksw.vshell.registry.JvmCommandRegistry;
import org.aksw.vshell.shim.rdfconvert.JvmCommandTranscode;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Test;

public class TestCommandRegistry {
    @Test
    public void test01() {
        System.setProperty("testcontainers.retryCount", "1");

        CompressorStreamFactory csf = new CompressorStreamFactory();

        JvmCommandRegistry jvmCmdRegistry = new JvmCommandRegistry();
        JvmCommand bzip2Cmd = JvmCommandTranscode.of(csf, CompressorStreamFactory.BZIP2);
        jvmCmdRegistry.put("/jvm/bzip2", bzip2Cmd);
        // jvmCmdRegistry.put("/virt/bzip2", bzip2Cmd);

        CommandRegistryImpl candidates = new CommandRegistryImpl();
        candidates.put("/virt/lbzip2", ExecSites.docker("nestio/lbzip2"), "/usr/bin/lbzip2");

        // Note: There can be multiple candidates per exec site.
        candidates.put("/virt/lbzip2", ExecSites.host(), "/usr/bin/lbzip2");
        candidates.put("/virt/lbzip2", ExecSites.jvm(), "/jvm/bzip2");

        CommandAvailability cmdAvailability = new CommandAvailability();
        // TODO Have image introspector write into cmdAvailability without having to know about exec sites.
        // Need an adapter or cmdAvailability.asDockerImageMap().

        Model shellModel = RDFDataMgr.loadModel("shell-ontology.ttl");
        ImageIntrospector imageIntrospector = ImageIntrospectorImpl.of(shellModel, cmdAvailability);
        imageIntrospector = new ImageIntrospectorCaching(imageIntrospector);

        ExecSiteResolver resolver = new ExecSiteResolver(candidates, jvmCmdRegistry,
            cmdAvailability, imageIntrospector);

        // execSiteResolver.providesCommand(null, null)
        // CommandRegistry dockerRegistry = new CommandRe

        System.out.println(resolver.resolve("/virt/lbzip2"));
        CmdOpExec cmdOp = CmdOpExec.ofLiterals("/virt/lbzip2", "-d");

        ExecSite qleverExecSite = ExecSites.docker("adfreiburg/qlever:commit-a307781");
        CommandPlacer commandPlacer = new CommandPlacer(candidates, resolver, Set.of(qleverExecSite));
        PlacedCommand placedCommand = commandPlacer.visit(cmdOp);
        System.out.println(placedCommand);

        // System.out.println(resolver.resolve("/usr/bin/lbzip2"));

        // CommandRegistry hostRegistry = new CommandRegistryOverLocator(ExecSiteCurrentHost.get(), new CommandLocatorHost());
        // CommandRegistry jvmRegistry = new CommandRegistryOverLocator(ExecSites.jvm(), new CommandLocatorJvmRegistry(jvmCmdRegistry));
        // CommandRegistry baseRegistry = new CommandRegistryUnion(List.of(jvmRegistry, candidates, hostRegistry));
    }
}
