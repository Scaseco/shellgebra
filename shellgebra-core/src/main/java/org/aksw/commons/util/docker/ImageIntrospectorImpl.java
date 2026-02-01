package org.aksw.commons.util.docker;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import com.google.common.base.Converter;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

import org.aksw.commons.collections.ConvertingSet;
import org.aksw.shellgebra.exec.CmdStrOpsBash;
import org.aksw.shellgebra.exec.SysRuntimeCore;
import org.aksw.shellgebra.exec.SysRuntimeCoreDocker;
import org.aksw.shellgebra.exec.model.ExecSite;
import org.aksw.shellgebra.exec.model.ExecSites;
import org.aksw.shellgebra.introspect.LocatorCommand2;
import org.aksw.shellgebra.introspect.ShellCatalogEntry;
import org.aksw.shellgebra.introspect.ShellProbeResult;
import org.aksw.shellgebra.introspect.ShellProbeResult.ShellProbeResultBuilder;
import org.aksw.shellgebra.model.osreo.ImageIntrospector;
import org.aksw.vshell.registry.ExecSiteProbeResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.ContainerFetchException;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.GenericContainer;

public class ImageIntrospectorImpl implements ImageIntrospector {
    // record CmdAvailability(String command, Entrypoint entrypoint, Boolean);

    private static final Logger logger = LoggerFactory.getLogger(ImageIntrospectorImpl.class);

    protected List<ShellCatalogEntry> shellCatalog;
    protected List<LocatorCommand2> locatorCatalog;

    protected Table<String, Argv, Boolean> imageToEntrypoints = HashBasedTable.create();
    // protected Table<Entrypoint, String, Boolean> cmdAvailability =
    // HashBasedTable.create();

    // Not ideal having this class here because it uses ExecSite and is more high
    // level.
    // Also, we should track metadata for (imageRef, command): the used entry point
    // and commandOption for the availability.
    protected ExecSiteProbeResults cmdAvailability;

//    public static ImageIntrospector of(Model osreoModel) {
//        return of(osreoModel, ExecSiteProbeResults.get());
//    }
//
//    public static ImageIntrospector of(Model osreoModel, ExecSiteProbeResults cmdAvailability) {
//        List<Shell> shells = OsreoUtils.listShells(osreoModel);
//        List<LocatorCommand> locatorCommands = OsreoUtils.listLocatorCommands(osreoModel);
//        return new ImageIntrospectorImpl(shells, locatorCommands, cmdAvailability);
//    }

//    public static ImageIntrospector of(ShellCatalog shellCatalog, ExecSiteProbeResults cmdAvailability) {
//        List<Shell> shells = OsreoUtils.listShells(osreoModel);
//        List<LocatorCommand> locatorCommands = OsreoUtils.listLocatorCommands(osreoModel);
//        return new ImageIntrospectorImpl(shells, locatorCommands, cmdAvailability);
//    }

    public static ImageIntrospector of() {
        List<ShellCatalogEntry> shellCatalog = getShellCatalog();
        List<LocatorCommand2> locatorCatalog = getLocatorCatalog();
        ExecSiteProbeResults execSiteProbeResults = ExecSiteProbeResults.get();
        return new ImageIntrospectorImpl(shellCatalog, locatorCatalog, execSiteProbeResults);
    }

    public ImageIntrospectorImpl(List<ShellCatalogEntry> shellCatalog, List<LocatorCommand2> locatorCatalog,
            ExecSiteProbeResults cmdAvailability) {
        super();
        this.shellCatalog = shellCatalog;
        this.locatorCatalog = locatorCatalog;
        this.cmdAvailability = cmdAvailability;
    }

    /** Mutable set view over an image's entry points. */
    public Set<Argv> getKnownEntryPoints(String imageName) {
        Set<Entry<Argv, Boolean>> a = imageToEntrypoints.row(imageName).entrySet();
        Set<Entry<Argv, Boolean>> b = Sets.filter(a, x -> Boolean.TRUE.equals(x.getValue()));
        Set<Argv> result = new ConvertingSet<>(b, Converter.from(Entry::getKey, x -> Map.entry(x, true)));
        return result;
    }

//    @Override
//    public ImageIntrospection findShell(String image, boolean pullIfAbsent) {
//        ImageIntrospection result = ModelFactory.createDefaultModel()
//            .createResource().as(ImageIntrospection.class);
//
//        if (ImageUtils.imageExists(image, pullIfAbsent)) {
//            findShell(result, image);
//        }
//
//        // Try without entry point (perhaps don't do this?)
//
//        return result;
//    }

    // public Entrypoint toEntrypoint(String entrypoint, List<String>
    // commandOptions)

    public static boolean canRunEntrypoint(String imageName, Argv ep, Table<String, Argv, Boolean> imageToEntrypoints,
            ExecSiteProbeResults cmdAvailability) {
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
        int exitCode = sysRuntime.runCmd(new String[] { command });
        return exitCode != 127;
    }

    public boolean hasCommand(SysRuntimeCoreDocker sysRuntime, String command) {
        String imageName = sysRuntime.getImageRef();
        ExecSite execSite = ExecSites.docker(imageName);
        boolean result = hasCommand(sysRuntime, command, execSite);
        return result;
    }

    public boolean hasCommand(SysRuntimeCore sysRuntime, String command, ExecSite execSite) {
        Boolean result = cmdAvailability.get(command, execSite);
        if (result == null) {
            try {
                // result = sysRuntime.runCmd(new String[]{command});
                result = hasCommandRaw(sysRuntime, command);
                // result = ContainerUtils.hasCommand(imageName, shellLocation, commandOptions,
                // command);
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

//    protected void findShell(ImageIntrospection result, String imageName) {
//        for (Shell shell : shellCatalog) {
//            findShell(result, imageName, shell);
//        }
//    }
//
//    protected void findShell(ImageIntrospection result, String imageName, String shellName) {
//        List<Shell> matches = shellCatalog.stream().filter(sh -> sh.getLabel().equals(shellName)).toList();
//        for (Shell match : matches) {
//            findShell(result, imageName, match);
//        }
//    }

//  boolean canRunEntrypoint = canRunEntrypoint(imageName, entrypoint);
//  logger.info("Probe image [{}] for shell [{}]: {}", imageName, shellName,
//      (canRunEntrypoint ? "" : "not ") + " found");
//  if (canRunEntrypoint) {
//      ShellSupport sh = result.getShellStatus().computeIfAbsent(shellName, key -> {
////          return result.getModel().createResource().as(ShellSupport.class);
//          return result.getOrCreateShellSupport(key);
//      });
    // Argv entrypoint = Argv.ofArgs(shellLocation, commandOptions);
    // sh.setShellType(shell);
    // sh.getShellType()
    // sh.setCommandOption(shell.getCommandOption());
    // sh.setCommandOption(shell.commandOption());
    // TODO Try to keep the container alive with the given entry point.
    // try (SysRuntimeCoreDocker runtime = findKeepAlive(imageName, entrypoint,
    // cmdAvailability)) {
    // Check for the locator command

//    public static void findShell(String imageName, ShellCatalogEntry shell) {
//        List<Argv> keepAlives = getKeepAliveCatalog();
//        boolean canRunEntrypoint = canRunEntrypoint(imageName, entrypoint);
//
//    }

    @Override
    public ShellProbeResult findShell(SysRuntimeCoreDocker runtime, ShellCatalogEntry shell) {
        String imageName = runtime.getImageRef();
        String shellName = shell.name();
        // List<String> commandOptions = Arrays.asList(shell.commandOption());
        logger.info("Probing image [{}] for shell [{}]", imageName, shellName);
        Collection<String> shellProbeLocations = shell.probeLocations();
        ShellProbeResultBuilder builder = ShellProbeResult.newBuilder();
        builder.setName(shellName);

        ShellProbeResult result = null;
        builder.setCommandOption(shell.commandOption());
        for (String shellLocation : shellProbeLocations) {

            boolean hasCommand = hasCommand(runtime, shellLocation);
            if (!hasCommand) {
                continue;
            }

            builder.setLocation(shellLocation);


            String builtInLocator = shell.builtInLocator();
            if (builtInLocator != null) {
                builder.setLocatorCommand(builtInLocator); // TODO Verify?
            } else {
                locatorProbe: for (LocatorCommand2 locatorCommand : locatorCatalog) {
                    for (String locatorLocation : locatorCommand.probeLocations()) {
                        boolean hasLocatorCommand = hasCommand(runtime, locatorLocation);
//                            logger.info("Probe locator [{}] for shell [{}] with option [{}] using locator [{}]: {}", imageName, shellLocation, commandOptions, locatorLocation,
//                                    (canRunEntrypoint ? "" : "not ") + " found");

                        if (hasLocatorCommand) {
                            builder.setLocatorCommand(locatorLocation);
                            break locatorProbe;
                        }
                    }
                }
            }
            // break outer; // This would stop after finding the first shell.
            result = builder.build();
            break;
        }
        return result;
    }

    public static SysRuntimeCoreDocker findKeepAlive(String imageRef, boolean pullIfAbsent,
            List<ShellCatalogEntry> shellCatalog, Table<String, Argv, Boolean> imageToEntrypoints,
            ExecSiteProbeResults probeResults) {
        // Table<String, Argv, Boolean> imageToEntrypoints = HashBasedTable.create();
        SysRuntimeCoreDocker result = null;
        List<Argv> keepAliveCatalog = getKeepAliveCatalog();
        for (ShellCatalogEntry shellEntry : shellCatalog) {
            for (String shellProbeLocation : shellEntry.probeLocations()) {
                Argv entrypoint = Argv.ofArgs(shellProbeLocation, shellEntry.commandOption());
                boolean canRunEntrypoint = canRunEntrypoint(imageRef, entrypoint, imageToEntrypoints, probeResults);
                if (canRunEntrypoint) {
                    result = startKeptAlive(imageRef, entrypoint, keepAliveCatalog, probeResults);
                    if (result != null) {
                        break;
                    }
                }
            }
        }
        return result;
    }

    public static SysRuntimeCoreDocker findKeepAlive(String imageRef, Argv entrypoint,
            ExecSiteProbeResults probeResults) {
        List<Argv> keepAliveCatalog = getKeepAliveCatalog();
        SysRuntimeCoreDocker result = startKeptAlive(imageRef, entrypoint, keepAliveCatalog, probeResults);
        return result;
    }

//    public static SysRuntimeCoreDocker findKeepAlive(String imageRef, Argv entrypoint, List<Argv> keepAliveCatalog, ExecSiteProbeResults probeResults) {
//        // List<Argv> keepAliveCatalog = getKeepAliveCatalog();
//        SysRuntimeCoreDocker result = startKeptAlive(imageRef, entrypoint, keepAliveCatalog, probeResults);
//        return result;
//    }

    // public GenericContainer<?> startKeptAlive(DockerImageName image) {

    private static Entry<Argv, Argv> merge(Argv entrypoint, Argv command) {
        Argv actualEntryPoint = entrypoint;
        Argv actualCommand = command;
        if (entrypoint != null && entrypoint.isEmpty()) {
            actualEntryPoint = Argv.of(command.command());
            actualCommand = Argv.of(command.args());
        }
        return Map.entry(actualEntryPoint, actualCommand);
    }

    public static SysRuntimeCoreDocker startKeptAlive(String imageRef, List<Argv> candKeepAliveCmds,
            ExecSiteProbeResults probeResults) {
        ExecSite execSite = ExecSites.docker(imageRef);
        Exception last = null;
        SysRuntimeCoreDocker result = null;
        for (Argv keepAliveArgv : candKeepAliveCmds) {
            // Skip re-checking of known unavailable argvs.
            if (probeResults.isKnownUnavailable(keepAliveArgv, execSite)) {
                continue;
            }

            String actualEntryPoint = keepAliveArgv.command();
            String[] actualCmd = keepAliveArgv.argsToArray();
            // Move any entrypoint arguments to the beginning of the command for consistency
            // with CLI:
            // Works: docker run --rm --entrypoint /usr/bin/sh ubuntu:24.04 -c 'which which'
            // Not possible: docker run --rm --entrypoint '/usr/bin/sh -c' ubuntu:24.04
            // 'which which'

            // String str = String.join(" ", keepAliveArgv.toArray());
            // String[] finalCmd =
            // ListBuilder.ofString().addAll(entrypoint.args()).add(str).buildArray();
            try {
                GenericContainer<?> c = new GenericContainer<>(imageRef);

                if (actualEntryPoint != null) {
                    c = c.withCreateContainerCmdModifier(x -> x.withEntrypoint(actualEntryPoint));
                }

                c = c.withCommand(actualCmd);

                c.start();
                // Argv actualEntryPointArgv = Argv.of(actualCmd);
                Argv actualEntryPointArgv = Argv.of(actualEntryPoint);
                result = new SysRuntimeCoreDocker(c, actualEntryPointArgv, CmdStrOpsBash.get());
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
                "Could not find a portable keep-alive command (image may be distroless/scratch).", last);
    }

    /**
     * Note: Generally, an entry point may be needed to run a keep alive command.
     * The options are:
     * <ul>
     * <li>Null entry point: does not override the image's entry point. The command
     * is set as usual.</li>
     * <li>Empty list entry point: The first element of the keep alive command
     * becomes the entry point.
     * <li>Non-empty list entry point: The entry point is set as usual.
     * </ul>
     *
     * Issue: if the entry point is a shell then the keep alive arguments need to
     * become an argument to the shell.
     *
     * @param imageRef
     * @param entrypoint        Any args of the entrypoint are prepended to the keep
     *                          alive command.
     * @param candKeepAliveCmds
     * @param probeResults
     * @return
     */
    @Deprecated // Mixing entry point and keep alive cmds makes things complicated - probably
                // unnecessarily.
    public static SysRuntimeCoreDocker startKeptAlive(String imageRef, Argv entrypoint, List<Argv> candKeepAliveCmds,
            ExecSiteProbeResults probeResults) {
        ExecSite execSite = ExecSites.docker(imageRef);
        Exception last = null;
        SysRuntimeCoreDocker result = null;
        for (Argv keepAliveArgv : candKeepAliveCmds) {
            // Skip re-checking of known unavailable argvs.
            if (probeResults.isKnownUnavailable(keepAliveArgv, execSite)) {
                continue;
            }

            Entry<Argv, Argv> entry = merge(entrypoint, keepAliveArgv);
            Argv actualEntryPoint = entry.getKey();
            Argv actualCommand = entry.getValue();

            // Move any entrypoint arguments to the beginning of the command for consistency
            // with CLI:
            // Works: docker run --rm --entrypoint /usr/bin/sh ubuntu:24.04 -c 'which which'
            // Not possible: docker run --rm --entrypoint '/usr/bin/sh -c' ubuntu:24.04
            // 'which which'

            // String str = String.join(" ", keepAliveArgv.toArray());
            // String[] finalCmd =
            // ListBuilder.ofString().addAll(entrypoint.args()).add(str).buildArray();
            try {
                GenericContainer<?> c = new GenericContainer<>(imageRef);

                if (actualEntryPoint != null) {
                    c = c.withCreateContainerCmdModifier(x -> x.withEntrypoint(actualEntryPoint.toArray()));
                }

                c = c.withCommand(actualCommand.toArray());

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
                "Could not find a portable keep-alive command (image may be distroless/scratch).", last);
    }

    /**
     *
     * @param imageRef
     * @param entrypoint        Any args of the entrypoint are prepended to the keep
     *                          alive command.
     * @param candKeepAliveCmds
     * @param probeResults
     * @return
     */
//   public static SysRuntimeCoreDocker startKeptAlive(String imageRef, List<Argv> candKeepAliveCmds, ExecSiteProbeResults probeResults) {
//       Exception last = null;
//       ExecSite execSite = ExecSites.docker(imageRef);
//
//       SysRuntimeCoreDocker result = null;
//       for (Argv keepAliveArgv : candKeepAliveCmds) {
//           // Skip re-checking of known unavailable argvs.
//           if (probeResults.isKnownUnavailable(keepAliveArgv, execSite)) {
//               continue;
//           }
//
//           // Move any entrypoint arguments to the beginning of the command for consistency with CLI:
//           // Works: docker run --rm --entrypoint /usr/bin/sh ubuntu:24.04 -c 'which which'
//           // Not possible: docker run --rm --entrypoint '/usr/bin/sh -c' ubuntu:24.04 'which which'
//
//           String str = String.join(" ", keepAliveArgv.toArray());
//           String actualEntryPoint =
//
//           String[] finalCmd = ListBuilder.ofString().addAll(entrypoint.args()).add(str).buildArray();
//           try {
//               GenericContainer<?> c = new GenericContainer<>(imageRef)
//                       .withCreateContainerCmdModifier(x -> x.withEntrypoint(actualEntryPoint))
//                       .withCommand(finalCmd);
//               c.start();
//               result = new SysRuntimeCoreDocker(c, entrypoint, CmdStrOpsBash.get());
//               probeResults.put(keepAliveArgv, execSite, true);
//               break;
//           } catch (Exception e) {
//               probeResults.put(keepAliveArgv, execSite, false);
//               last = e; // try next
//           }
//       }
//
//       if (result != null) {
//           return result;
//       }
//
//       throw new IllegalStateException(
//           "Could not find a portable keep-alive command (image may be distroless/scratch).",
//           last
//       );
//   }

    public static List<Argv> getKeepAliveCatalog() {
        List<Argv> candidates = Arrays.asList(new String[][] {
                // GNU coreutils (PATH lookup)
                // BusyBox/Alpine (PATH lookup)
                { "sleep", "365d" }, { "/bin/sleep", "365d" }, { "/usr/bin/sleep", "365d" },
                // Absolute paths (cover usrmerge + busybox)
                { "sleep", "infinity" }, { "/bin/sleep", "infinity" }, { "/usr/bin/sleep", "infinity" },
                // Tail fallback (very common)
                { "tail", "-f", "/dev/null" }, { "/bin/tail", "-f", "/dev/null" },
                { "/usr/bin/tail", "-f", "/dev/null" },
                // If BusyBox is present as a single binary:
                { "/bin/busybox", "sleep", "365d" }, { "/usr/bin/busybox", "sleep", "365d" },
                // Last-resort shell loop IF a shell exists
                { "sh", "-c", "while :; do sleep 1h; done" }, { "/bin/sh", "-c", "while :; do sleep 1h; done" },
                { "/usr/bin/sh", "-c", "while :; do sleep 1h; done" } }).stream().map(Argv::of).toList();
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

    public static List<ShellCatalogEntry> getShellSubCatalog(List<ShellCatalogEntry> catalog, String shellName) {
        List<ShellCatalogEntry> result = catalog.stream().filter(entry -> Objects.equals(entry.name(), shellName))
                .toList();
        return result;
    }

    public static List<ShellCatalogEntry> getShellCatalog() {
        List<ShellCatalogEntry> result = List.of(
            // new ShellCatalogEntry("bash", List.of("/bin/bash", "/usr/bin/bash"), "-c", null)
            new ShellCatalogEntry("bash", List.of("/bin/bash", "/usr/bin/bash", "/bin/sh", "/usr/bin/sh"), "-c", null)
        );
        return result;
    }

    public static List<LocatorCommand2> getLocatorCatalog() {
        List<LocatorCommand2> result = List.of(new LocatorCommand2("which", List.of("/bin/which", "/usr/bin/which")));
        return result;
    }
}
