package org.aksw.commons.util.docker;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Converter;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

import org.aksw.commons.collections.ConvertingSet;
import org.aksw.jenax.model.osreo.ImageIntrospection;
import org.aksw.jenax.model.osreo.LocatorCommand;
import org.aksw.jenax.model.osreo.OsreoUtils;
import org.aksw.jenax.model.osreo.Shell;
import org.aksw.jenax.model.osreo.ShellSupport;
import org.aksw.shellgebra.exec.CmdStrOpsBash;
import org.aksw.shellgebra.exec.ListBuilder;
import org.aksw.shellgebra.exec.SysRuntimeCore;
import org.aksw.shellgebra.exec.SysRuntimeCoreDocker;
import org.aksw.shellgebra.exec.model.ExecSite;
import org.aksw.shellgebra.exec.model.ExecSites;
import org.aksw.vshell.registry.ExecSiteProbeResults;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.ContainerFetchException;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.GenericContainer;

public class ImageIntrospectorImpl
    implements ImageIntrospector
{



    // record CmdAvailability(String command, Entrypoint entrypoint, Boolean);

    private static final Logger logger = LoggerFactory.getLogger(ImageIntrospectorImpl.class);
    protected Model model;

    protected List<Shell> shellCatalog;
    protected List<LocatorCommand> locatorCatalog;

    protected Table<String, Argv, Boolean> imageToEntrypoints = HashBasedTable.create();
    // protected Table<Entrypoint, String, Boolean> cmdAvailability = HashBasedTable.create();

    // Not ideal having this class here because it uses ExecSite and is more high level.
    // Also, we should track metadata for (imageRef, command): the used entry point and commandOption for the availability.
    protected ExecSiteProbeResults cmdAvailability;

    public static ImageIntrospector of(Model osreoModel, ExecSiteProbeResults cmdAvailability) {
        List<Shell> shells = OsreoUtils.listShells(osreoModel);
        List<LocatorCommand> locatorCommands = OsreoUtils.listLocatorCommands(osreoModel);
        return new ImageIntrospectorImpl(shells, locatorCommands, cmdAvailability);
    }

    public ImageIntrospectorImpl(List<Shell> shells, List<LocatorCommand> locatorCommands, ExecSiteProbeResults cmdAvailability) {
        super();
        this.shellCatalog = shells;
        this.locatorCatalog = locatorCommands;
        this.cmdAvailability = cmdAvailability;
    }

    /** Mutable set view over an image's entry points. */
    public Set<Argv> getKnownEntryPoints(String imageName) {
        Set<Entry<Argv, Boolean>> a = imageToEntrypoints.row(imageName).entrySet();
        Set<Entry<Argv, Boolean>> b = Sets.filter(a, x -> Boolean.TRUE.equals(x.getValue()));
        Set<Argv> result = new ConvertingSet<>(b, Converter.from(Entry::getKey, x -> Map.entry(x, true)));
        return result;
    }

    @Override
    public ImageIntrospection inspect(String image, boolean pullIfAbsent) {
        ImageIntrospection result = ModelFactory.createDefaultModel()
            .createResource().as(ImageIntrospection.class);

        if (ImageUtils.imageExists(image, pullIfAbsent)) {
            findShell(result, image);
        }

        // Try without entry point (perhaps don't do this?)

        return result;
    }

    // public Entrypoint toEntrypoint(String entrypoint, List<String> commandOptions)

    public boolean canRunEntrypoint(String imageName, Argv ep) {
//         Entrypoint ep = new Entrypoint(entrypoint, commandOptions);
        Boolean result = imageToEntrypoints.get(imageName, ep);
        if (result == null) {
            try {
                result = ContainerUtils.canRunEntrypoint(imageName, ep.command(), ep.args());
            } catch (ContainerFetchException | ContainerLaunchException e) {
                result = false;
            }
            imageToEntrypoints.put(imageName, ep, result);
            // Also declare the entrypoint as an available command
            // cmdAvailability.put(ep, entrypoint, result);
            cmdAvailability.put(ep.command(), ExecSites.docker(imageName), result);
        }
        return result;
    }

    public boolean hasCommandRaw(SysRuntimeCore sysRuntime, String command) throws InterruptedException, IOException {
        int exitCode = sysRuntime.runCmd(new String[]{command});
        return exitCode != 127;
    }

    public boolean hasCommand(SysRuntimeCoreDocker sysRuntime, String command) {
        String imageName = sysRuntime.getImageRef();
        ExecSite execSite = ExecSites.docker(imageName);
        Boolean result = cmdAvailability.get(command, execSite);
        if (result == null) {
            try {
                // result = sysRuntime.runCmd(new String[]{command});
                result = hasCommandRaw(sysRuntime, command);
                // result = ContainerUtils.hasCommand(imageName, shellLocation, commandOptions, command);
                cmdAvailability.put(command, execSite, result);
            } catch (ContainerFetchException | ContainerLaunchException e) {
                result = false;
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    @Deprecated
    public boolean hasCommand(String imageName, String shellLocation, List<String> commandOptions, String command) {
        ExecSite execSite = ExecSites.docker(imageName);
        Boolean result = cmdAvailability.get(command, execSite);
        if (result == null) {
            try {
                result = ContainerUtils.hasCommand(imageName, shellLocation, commandOptions, command);
                cmdAvailability.put(command, execSite, result);
            } catch (ContainerFetchException | ContainerLaunchException e) {
                result = false;
            }
        }
        return result;
    }

    protected void findShell(ImageIntrospection result, String imageName) {
        for (Shell shell : shellCatalog) {
            findShell(result, imageName, shell);
        }
    }

    protected void findShell(ImageIntrospection result, String imageName, String shellName) {
        List<Shell> matches = shellCatalog.stream().filter(sh -> sh.getLabel().equals(shellName)).toList();
        for (Shell match : matches) {
            findShell(result, imageName, match);
        }
    }

    protected void findShell(ImageIntrospection result, String imageName, Shell shell) {
        String shellName = shell.getLabel();
        List<String> commandOptions = Arrays.asList(shell.getCommandOption());

        logger.info("Probing image [{}] for shell [{}]" , imageName, shellName);

        Set<String> shellProbeLocations = shell.getProbeLocations();
        for (String shellLocation : shellProbeLocations) {
            Argv entrypoint = Argv.ofArgs(shellLocation, commandOptions);
            boolean canRunEntrypoint = canRunEntrypoint(imageName, entrypoint);

            logger.info("Probe image [{}] for shell [{}]: {}", imageName, shellName,
                (canRunEntrypoint ? "" : "not ") + " found");

            if (canRunEntrypoint) {
                ShellSupport sh = result.getShellStatus().computeIfAbsent(shellName, key -> {
                    return result.getModel().createResource().as(ShellSupport.class);
                });
                sh.setCommandPath(shellLocation);
                sh.setShellType(shell);
                sh.setCommandOption(shell.getCommandOption());

                // TODO Try to keep the container alive with the given entry point.
                try (SysRuntimeCoreDocker runtime = findKeepAlive(imageName, entrypoint, cmdAvailability)){
                    // Check for the locator command
                    String builtInLocator = shell.getLocatorCommand();
                    if (builtInLocator != null) {
                        sh.setLocatorCommand(builtInLocator); // TODO Verify?
                    } else {
                        for (LocatorCommand locatorCommand : locatorCatalog) {
                            for (String locatorLocation : locatorCommand.getProbeLocations()) {
                                boolean hasCommand = hasCommand(runtime,  locatorLocation);
                                logger.info("Probe locator [{}] for shell [{}] with option [{}] using locator [{}]: {}", imageName, shellLocation, commandOptions, locatorLocation,
                                        (canRunEntrypoint ? "" : "not ") + " found");

                                if (hasCommand) {
                                    sh.setLocatorCommand(locatorLocation);
                                }
                            }
                        }
                    }
                    // break outer; // This would stop after finding the first shell.
                    break;
                }
            }
        }
    }

    public static SysRuntimeCoreDocker findKeepAlive(String imageRef, Argv entrypoint, ExecSiteProbeResults probeResults) {
        List<Argv> keepAliveCatalog = getKeepAliveCatalog();
        SysRuntimeCoreDocker result = startKeptAlive(imageRef, entrypoint, keepAliveCatalog, probeResults);
        return result;
    }

    // public GenericContainer<?> startKeptAlive(DockerImageName image) {
    /**
     *
     * @param imageRef
     * @param entrypoint Any args of the entrypoint are prepended to the keep alive command.
     * @param candKeepAliveCmds
     * @param probeResults
     * @return
     */
    public static SysRuntimeCoreDocker startKeptAlive(String imageRef, Argv entrypoint, List<Argv> candKeepAliveCmds, ExecSiteProbeResults probeResults) {
        Exception last = null;
        // List<String[]> candidates = getKeepAliveCatalog();

        String actualEntryPoint = entrypoint.command();

        ExecSite execSite = ExecSites.docker(imageRef);

        SysRuntimeCoreDocker result = null;
        for (Argv keepAliveArgv : candKeepAliveCmds) {
            // Skip re-checking of known unavailable argvs.
            if (probeResults.isKnownUnavailable(keepAliveArgv, execSite)) {
                continue;
            }

            // Move any entrypoint arguments to the beginning of the command for consistency with CLI:
            // Works: docker run --rm --entrypoint /usr/bin/sh ubuntu:24.04 -c 'which which'
            // Not possible: docker run --rm --entrypoint '/usr/bin/sh -c' ubuntu:24.04 'which which'

            String str = String.join(" ", keepAliveArgv.toArray());

            String[] finalCmd = ListBuilder.forString().addAll(entrypoint.args()).add(str).buildArray();
            try {
                GenericContainer<?> c = new GenericContainer<>(imageRef)
                        .withCreateContainerCmdModifier(x -> x.withEntrypoint(actualEntryPoint))
                        .withCommand(finalCmd);
                c.start();
                result = new SysRuntimeCoreDocker(c, entrypoint, CmdStrOpsBash.get());
                probeResults.put(keepAliveArgv, execSite, true);
                break;
            } catch (Exception e) {
                probeResults.put(keepAliveArgv, execSite, false);
                last = e; // try next
            }
        }

        if (result != null) {
            return result;
        }

        throw new IllegalStateException(
            "Could not find a portable keep-alive command (image may be distroless/scratch).",
            last
        );
    }

    public static List<Argv> getKeepAliveCatalog() {
        List<Argv> candidates = Arrays.asList(new String[][]{
            // GNU coreutils (PATH lookup)
            // BusyBox/Alpine (PATH lookup)
            {"sleep", "365d"},
            {"/bin/sleep", "365d"},
            {"/usr/bin/sleep", "365d"},
            // Absolute paths (cover usrmerge + busybox)
            {"sleep", "infinity"},
            {"/bin/sleep", "infinity"},
            {"/usr/bin/sleep", "infinity"},
            // Tail fallback (very common)
            {"tail", "-f", "/dev/null"},
            {"/bin/tail", "-f", "/dev/null"},
            {"/usr/bin/tail", "-f", "/dev/null"},
            // If BusyBox is present as a single binary:
            {"/bin/busybox", "sleep", "365d"},
            {"/usr/bin/busybox", "sleep", "365d"},
            // Last-resort shell loop IF a shell exists
            {"sh", "-c", "while :; do sleep 1h; done"},
            {"/bin/sh", "-c", "while :; do sleep 1h; done"},
            {"/usr/bin/sh", "-c", "while :; do sleep 1h; done"}
        }).stream().map(Argv::of).toList();
//        List<String[]> candidates = Arrays.asList(new String[][]{
//            // GNU coreutils (PATH lookup)
//            {"sleep", "infinity"},
//            // BusyBox/Alpine (PATH lookup)
//            {"sleep", "365d"},
//            // Absolute paths (cover usrmerge + busybox)
//            {"/bin/sleep", "infinity"},
//            {"/usr/bin/sleep", "infinity"},
//            {"/bin/sleep", "365d"},
//            {"/usr/bin/sleep", "365d"},
//            // Tail fallback (very common)
//            {"tail", "-f", "/dev/null"},
//            {"/bin/tail", "-f", "/dev/null"},
//            {"/usr/bin/tail", "-f", "/dev/null"},
//            // If BusyBox is present as a single binary:
//            {"/bin/busybox", "sleep", "365d"},
//            {"/usr/bin/busybox", "sleep", "365d"},
//            // Last-resort shell loop IF a shell exists
//            {"sh", "-c", "while :; do sleep 1h; done"},
//            {"/bin/sh", "-c", "while :; do sleep 1h; done"},
//            {"/usr/bin/sh", "-c", "while :; do sleep 1h; done"}
//        });
        return candidates;
    }

}
