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
import org.aksw.commons.util.docker.ImageIntrospectorImpl;
import org.aksw.shellgebra.exec.SysRuntimeFactoryDocker;
import org.aksw.shellgebra.exec.SysRuntimeImpl;
import org.aksw.shellgebra.exec.model.ExecSite;
import org.aksw.shellgebra.exec.model.ExecSiteCurrentHost;
import org.aksw.shellgebra.exec.model.ExecSiteCurrentJvm;
import org.aksw.shellgebra.exec.model.ExecSiteDockerImage;
import org.aksw.shellgebra.exec.model.ExecSiteVisitor;
import org.aksw.shellgebra.introspect.ShellProbeResult;
import org.aksw.shellgebra.model.osreo.ImageIntrospector;
import org.aksw.shellgebra.shim.core.JvmCommand;
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

    public static ExecSiteResolver of(CommandCatalog commandCatalog, JvmCommandRegistry jvmCmdRegistry) {
        // Model model = RDFDataMgr.loadModel("shell-ontology.ttl");
        ImageIntrospector imageIntrospector = ImageIntrospectorImpl.of();
        return new ExecSiteResolver(commandCatalog, jvmCmdRegistry, ExecSiteProbeResults.get(), imageIntrospector);
    }

    public CommandCatalog getCommandCatalog() {
        return cmdCatalog;
    }

    // XXX Should this method exist or is it API creep?
    public JvmCommandRegistry getJvmCmdRegistry() {
        return jvmCmdRegistry;
    }

    public Optional<CommandBinding> resolve(String virtualCmd, ExecSite execSite) {
        CommandBinding result = null;
        Set<CommandBinding> candLocations = cmdCatalog.get(virtualCmd, execSite).orElse(Set.of());
        for (CommandBinding cmdBinding : candLocations) {
            String cmdName = cmdBinding.commandName();
            boolean isPresent = providesCommand(cmdName, execSite);
            if (isPresent) {
                result = cmdBinding;
                break;
            }
        }
        return Optional.ofNullable(result);
    }

    public Map<ExecSite, CommandBinding> resolve(String virtualCmd) {
        Map<ExecSite, CommandBinding> result = new LinkedHashMap<>();
        Map<ExecSite, Collection<CommandBinding>> map = cmdCatalog.get(virtualCmd).asMap();
        for (Entry<ExecSite, Collection<CommandBinding>> e : map.entrySet()) {
            ExecSite execSite = e.getKey();
            for (CommandBinding cmdBinding : e.getValue()) {
                String cmdName = cmdBinding.commandName();
                boolean isPresent = providesCommand(cmdName, execSite);
                if (isPresent) {
                    result.put(e.getKey(), cmdBinding);
                }
            }
        }
        return result;
    }

    public boolean canRunPipeline(ExecSite execSite) {
        return execSite.accept(new ExecSiteVisitor<Boolean>() {
            @Override
            public Boolean visit(ExecSiteDockerImage execSite) {
//                ImageIntrospection insp = dockerImageIntrospector.findShell(execSite.imageRef(), true);
//                ShellSupport bash = insp.getShellStatus().get("bash");
//                return bash != null;
                ShellProbeResult probeResult = SysRuntimeFactoryDocker.get().findShell(execSite.imageRef(), true, "bash");
                return probeResult != null;
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
//                    ImageIntrospection ii = dockerImageIntrospector.findShell(imageRef, true);
//                    ShellSupport ss = ii.getShellStatus().get("bash");
                    ShellProbeResult probeResult = SysRuntimeFactoryDocker.get().findShell(execSite.imageRef(), true, "bash");

                    if (probeResult != null) {
                        entrypoint = probeResult.location();
                        commandPrefix = Optional.ofNullable(probeResult.commandOption())
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
