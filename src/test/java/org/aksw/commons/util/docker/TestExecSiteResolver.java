package org.aksw.commons.util.docker;

import org.aksw.shellgebra.exec.model.ExecSiteDockerImage;
import org.aksw.vshell.registry.CommandRegistry;
import org.aksw.vshell.registry.ExecSiteResolver;
import org.aksw.vshell.registry.JvmCommandRegistry;
import org.junit.Test;

public class TestExecSiteResolver {
    @Test
    public void testHost() {
        JvmCommandRegistry jvmCmdRegistry = JvmCommandRegistry.get();
        CommandRegistry cmdAvailability = new CommandRegistry();
        ExecSiteResolver execSiteResolver = new ExecSiteResolver(jvmCmdRegistry, cmdAvailability);
        // boolean b = execSiteResolver.providesCommand("/usr/bin/rapper", ExecSiteCurrentJvm.get());
        boolean b = execSiteResolver.providesCommand("/usr/bin/bash", new ExecSiteDockerImage("ubuntu:24.04"));

        // boolean b = execSiteResolver.providesCommand("/usr/bin/rapper", ExecSiteCurrentHost.get());
        System.out.println(b);

        b = execSiteResolver.providesCommand("/usr/bin/bash", new ExecSiteDockerImage("ubuntu:24.04"));

        // boolean b = execSiteResolver.providesCommand("/usr/bin/rapper", ExecSiteCurrentHost.get());
        System.out.println(b);

    }
}
