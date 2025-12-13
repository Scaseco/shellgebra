package org.aksw.vshell.registry;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.aksw.commons.util.docker.ContainerUtils;
import org.aksw.commons.util.docker.ImageIntrospector;
import org.aksw.jenax.model.osreo.ImageIntrospection;
import org.aksw.jenax.model.osreo.ShellSupport;
import org.aksw.shellgebra.exec.SysRuntimeImpl;
import org.aksw.shellgebra.exec.model.ExecSite;
import org.aksw.shellgebra.exec.model.ExecSiteCurrentHost;
import org.aksw.shellgebra.exec.model.ExecSiteCurrentJvm;
import org.aksw.shellgebra.exec.model.ExecSiteDockerImage;
import org.aksw.shellgebra.exec.model.ExecSiteVisitor;
import org.testcontainers.containers.ContainerFetchException;
import org.testcontainers.containers.ContainerLaunchException;

public class ExecSiteResolver {
    private CommandCatalog cmdCatalog;

    private JvmCommandRegistry jvmCmdRegistry;
    private ExecSiteProbeResults cmdAvailability;
    private ImageIntrospector dockerImageIntrospector;

    public ExecSiteResolver(CommandCatalog cmdCatalog, JvmCommandRegistry jvmCmdRegistry,
            ExecSiteProbeResults cmdAvailability, ImageIntrospector dockerImageIntrospector) {
        super();
        this.cmdCatalog = cmdCatalog;
        this.jvmCmdRegistry = jvmCmdRegistry;
        this.cmdAvailability = cmdAvailability;
        this.dockerImageIntrospector = dockerImageIntrospector;
    }

    public CommandCatalog getCommandCatalog() {
        return cmdCatalog;
    }

    // XXX Should this method exist or is it API creep?
    public JvmCommandRegistry getJvmCmdRegistry() {
        return jvmCmdRegistry;
    }

    public Optional<String> resolve(String virtualCmd, ExecSite execSite) {
        String result = null;
        Set<String> candLocations = cmdCatalog.get(virtualCmd, execSite).orElse(Set.of());
        for (String cmd : candLocations) {
            boolean isPresent = providesCommand(cmd, execSite);
            if (isPresent) {
                result = cmd;
                break;
            }
        }
        return Optional.ofNullable(result);
    }

    public Map<ExecSite, String> resolve(String virtualCmd) {
        Map<ExecSite, String> result = new LinkedHashMap<>();
        Map<ExecSite, Collection<String>> map = cmdCatalog.get(virtualCmd).asMap();
        for (Entry<ExecSite, Collection<String>> e : map.entrySet()) {
            ExecSite execSite = e.getKey();
            for (String cmd : e.getValue()) {
                boolean isPresent = providesCommand(cmd, execSite);
                if (isPresent) {
                    result.put(e.getKey(), cmd);
                }
            }
        }
        return result;
    }

    public boolean canRunPipeline(ExecSite execSite) {
        return execSite.accept(new ExecSiteVisitor<Boolean>() {
            @Override
            public Boolean visit(ExecSiteDockerImage execSite) {
                ImageIntrospection insp = dockerImageIntrospector.inspect(execSite.imageRef(),true);
                ShellSupport bash = insp.getShellStatus().get("bash");
                return bash != null;
            }

            @Override
            public Boolean visit(ExecSiteCurrentHost execSite) {
                // SysRuntimeImpl.forCurrentOs()execSite;
                return true;
            }

            @Override
            public Boolean visit(ExecSiteCurrentJvm execSite) {
                return true;
            }

        });
        // dockerImageIntrospector.inspect(execSite,
    }

    public boolean providesCommand(String command, ExecSite execSite) {
        // If jvm then check the jvm registry
        // if host then check the host
        // if docker then check the container.
        return execSite.accept(new ExecSiteVisitor<Boolean>() {
            @Override
            public Boolean visit(ExecSiteDockerImage execSite) {
                String imageRef = execSite.imageRef();
                List<String> commandPrefix = null;
                String entrypoint = null;
                Boolean r = cmdAvailability.get(command, execSite);
                if (r == null) {
                    // TODO: We should also check whether the command works without a shell
                    // e.g. if the default entry point already is a shell.
                    ImageIntrospection ii = dockerImageIntrospector.inspect(imageRef, true);
                    ShellSupport ss = ii.getShellStatus().get("bash");
                    if (ss != null) {
                        entrypoint = ss.getCommandPath();
                        commandPrefix = Optional.ofNullable(ss.getCommandOption())
                            .map(List::of).orElse(List.of());
                    }

                    try {
                        r = ContainerUtils.hasCommand(imageRef, entrypoint, commandPrefix, command);
                    } catch (ContainerFetchException | ContainerLaunchException e) {
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
