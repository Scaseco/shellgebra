package org.aksw.vshell.registry;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.aksw.commons.util.docker.ContainerUtils;
import org.aksw.shellgebra.exec.SysRuntimeImpl;
import org.aksw.shellgebra.exec.model.ExecSite;
import org.aksw.shellgebra.exec.model.ExecSiteCurrentHost;
import org.aksw.shellgebra.exec.model.ExecSiteCurrentJvm;
import org.aksw.shellgebra.exec.model.ExecSiteDockerImage;
import org.aksw.shellgebra.exec.model.ExecSiteVisitor;
import org.testcontainers.containers.ContainerFetchException;


public class ExecSiteResolver {
    private CommandRegistry candRegistry;

    private JvmCommandRegistry jvmCmdRegistry;
    private CommandAvailability cmdAvailability;

    public ExecSiteResolver(CommandRegistry candRegistry, JvmCommandRegistry jvmCmdRegistry, CommandAvailability cmdAvailability) {
        super();
        this.candRegistry = candRegistry;
        this.jvmCmdRegistry = jvmCmdRegistry;
        this.cmdAvailability = cmdAvailability;
    }

    public Map<ExecSite, String> resolve(String virtualCmd) {
        Map<ExecSite, String> result = new LinkedHashMap<>();
        Map<ExecSite, String> map = candRegistry.get(virtualCmd);
        for (Entry<ExecSite, String> e : map.entrySet()) {
            boolean isPresent = providesCommand(e.getValue(), e.getKey());
            if (isPresent) {
                result.put(e.getKey(), e.getValue());
            }
        }
        return result;
    }

    public boolean providesCommand(String command, ExecSite execSite) {
        // If jvm then check the jvm registry
        // if host then check the host
        // if docker then check the container.
        return execSite.accept(new ExecSiteVisitor<Boolean>() {
            @Override
            public Boolean visit(ExecSiteDockerImage execSite) {
                String[] commandPrefix = null;
                String entrypoint = null;
                Boolean r = cmdAvailability.get(command, execSite);
                if (r == null) {
                    try {
                        r = ContainerUtils.hasCommand(execSite.imageRef(), entrypoint, commandPrefix, new String[] {command});
                    } catch (ContainerFetchException e) {
                        r = false;
                    }
                    cmdAvailability.put(command, execSite, r);
                }
                return r;
            }

            @Override
            public Boolean visit(ExecSiteCurrentHost execSite) {
                boolean doCache = false;
                Boolean r = doCache ? cmdAvailability.get(command, execSite) : null;
                if (r == null) {
                    String path = null;
                    try {
                        path = SysRuntimeImpl.forCurrentOs().which(command);
                    } catch (IOException | InterruptedException e) {
                        // Ignored.
                    }
                    r = path != null;
                    if (doCache) {
                        cmdAvailability.put(command, execSite, r);
                    }
                }
                return r;
            }

            @Override
            public Boolean visit(ExecSiteCurrentJvm execSite) {
                Optional<JvmCommand> jvmCmd = jvmCmdRegistry.get(command);
                return jvmCmd.isPresent();
            }
        });
    }
}
