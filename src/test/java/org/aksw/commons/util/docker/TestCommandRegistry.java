package org.aksw.commons.util.docker;

import org.aksw.shellgebra.exec.model.ExecSites;
import org.aksw.vshell.registry.CommandAvailability;
import org.aksw.vshell.registry.CommandRegistryImpl;
import org.aksw.vshell.registry.ExecSiteResolver;
import org.aksw.vshell.registry.JvmCommand;
import org.aksw.vshell.registry.JvmCommandRegistry;
import org.aksw.vshell.shim.rdfconvert.JvmCommandTranscode;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.junit.Test;

public class TestCommandRegistry {
    @Test
    public void test01() {
        CompressorStreamFactory csf = new CompressorStreamFactory();

        JvmCommandRegistry jvmCmdRegistry = new JvmCommandRegistry();
        JvmCommand bzip2Cmd = JvmCommandTranscode.of(csf, CompressorStreamFactory.BZIP2);
        jvmCmdRegistry.put("/jvm/bzip2", bzip2Cmd);
        // jvmCmdRegistry.put("/virt/bzip2", bzip2Cmd);

        CommandRegistryImpl candidates = new CommandRegistryImpl();
        candidates.put("/virt/lbzip2", ExecSites.docker("nestio/lbzip2"), "/usr/bin/lbzip2");
        candidates.put("/virt/lbzip2", ExecSites.host(), "/usr/bin/lbzip2");
        candidates.put("/virt/lbzip2", ExecSites.jvm(), "/jvm/bzip2");

        CommandAvailability cmdAvailability = new CommandAvailability();
        ExecSiteResolver resolver = new ExecSiteResolver(candidates, jvmCmdRegistry, cmdAvailability);

        // execSiteResolver.providesCommand(null, null)

        // CommandRegistry dockerRegistry = new CommandRe

        System.out.println(resolver.resolve("/virt/lbzip2"));
        // System.out.println(resolver.resolve("/usr/bin/lbzip2"));

        // CommandRegistry hostRegistry = new CommandRegistryOverLocator(ExecSiteCurrentHost.get(), new CommandLocatorHost());
        // CommandRegistry jvmRegistry = new CommandRegistryOverLocator(ExecSites.jvm(), new CommandLocatorJvmRegistry(jvmCmdRegistry));
        // CommandRegistry baseRegistry = new CommandRegistryUnion(List.of(jvmRegistry, candidates, hostRegistry));
    }
}
