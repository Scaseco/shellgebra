package org.aksw.shellgebra.registry.init;

import org.aksw.shellgebra.exec.model.ExecSites;
import org.aksw.shellgebra.shim.cmd.JvmCommandCat;
import org.aksw.shellgebra.shim.cmd.JvmCommandEcho;
import org.aksw.shellgebra.shim.cmd.JvmCommandHead;
import org.aksw.shellgebra.shim.cmd.JvmCommandTranscode;
import org.aksw.shellgebra.shim.cmd.JvmCommandWhich;
import org.aksw.shellgebra.shim.core.JvmCommand;
import org.aksw.vshell.registry.CommandRegistry;
import org.aksw.vshell.registry.JvmCommandRegistry;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

public class InitCommandRegistry {
    /** These are the physical jvms. */
    public static JvmCommandRegistry initJvmCmdRegistry(JvmCommandRegistry jvmCmdRegistry) {
        // Core command: which - resolve short name to fully qualified command name.
        jvmCmdRegistry.put("/bin/which", new JvmCommandWhich());

        // Core command: test - return 0 if name is fully qualified command name - 1 otherwise.
        // jvmCmdRegistry.put("/bin/test", new JvmCmdTest());

        jvmCmdRegistry.put("/bin/echo", new JvmCommandEcho());
        jvmCmdRegistry.put("/bin/cat", new JvmCommandCat());
        jvmCmdRegistry.put("/usr/bin/echo", new JvmCommandEcho());
        jvmCmdRegistry.put("/usr/bin/cat", new JvmCommandCat());
        jvmCmdRegistry.put("/bin/head", new JvmCommandHead());

        CompressorStreamFactory csf = new CompressorStreamFactory();

        JvmCommand bzip2Cmd = JvmCommandTranscode.of(csf, CompressorStreamFactory.BZIP2);
        jvmCmdRegistry.put("/jvm/bzip2", bzip2Cmd);
        return jvmCmdRegistry;
    }

    /*
     * XXX Should we indirectly access the command parser via the jvm exec site?!
     *     Or should the jvm parser be linked to the virt command?
     */

    /** These are the candidate mappings. */
    public static CommandRegistry initCmdCandRegistry(CommandRegistry registry) {
        registry.put("/virt/lbzip2", ExecSites.docker("nestio/lbzip2"), "/usr/bin/lbzip2");
        // Note: There can be multiple candidates per exec site.
        registry.put("/virt/lbzip2", ExecSites.host(), "/usr/bin/lbzip2");
        registry.put("/virt/lbzip2", ExecSites.jvm(), "/jvm/bzip2");

        registry.put("/virt/bzip2", ExecSites.jvm(), "/jvm/bzip2");

        registry.put("/virt/echo", ExecSites.host(), "/usr/bin/echo");
        registry.put("/virt/echo", ExecSites.jvm(), "/bin/echo");
        registry.put("/usr/bin/cat", ExecSites.host(), "/usr/bin/cat");

        registry.put("/virt/cat", ExecSites.jvm(), "/bin/cat");
        registry.put("/virt/cat", ExecSites.host(), "/bin/cat");
        return registry;
    }
}
