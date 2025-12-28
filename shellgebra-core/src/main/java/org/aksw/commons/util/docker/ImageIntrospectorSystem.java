package org.aksw.commons.util.docker;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.jenax.model.osreo.ImageIntrospection;
import org.aksw.jenax.model.osreo.LocatorCommand;
import org.aksw.jenax.model.osreo.OsreoUtils;
import org.aksw.jenax.model.osreo.Shell;
import org.aksw.jenax.model.osreo.ShellSupport;
import org.aksw.shellgebra.exec.SysRuntimeImpl;
import org.aksw.shellgebra.exec.model.ExecSite;
import org.aksw.shellgebra.exec.model.ExecSites;
import org.aksw.vshell.registry.ExecSiteProbeResults;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.ContainerFetchException;
import org.testcontainers.containers.ContainerLaunchException;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class ImageIntrospectorSystem
    implements ImageIntrospector
{
    record Entrypoint(String imageName, String command, List<String> options) {}

    // record CmdAvailability(String command, Entrypoint entrypoint, Boolean);

    private static final Logger logger = LoggerFactory.getLogger(ImageIntrospectorImpl.class);
    protected Model model;

    protected List<Shell> shells;
    protected List<LocatorCommand> locatorCommands;

    protected Table<String, Entrypoint, Boolean> imageToEntrypoints = HashBasedTable.create();

    // protected Map<String, Map<Entrypoint, Boolean>> imageToEntrypoints;
    // protected Map<String, Table<Entrypoint, String, Boolean>> imageToKeepAlive;


    // protected Table<Entrypoint, String, Boolean> cmdAvailability = HashBasedTable.create();

    // Not ideal having this class here because it uses ExecSite and is more high level.
    // Also, we should track metadata for (imageRef, command): the used entry point and commandOption for the availability.
    protected ExecSiteProbeResults cmdAvailability;

    public static ImageIntrospector of(Model osreoModel, ExecSiteProbeResults cmdAvailability) {
        List<Shell> shells = OsreoUtils.listShells(osreoModel);
        List<LocatorCommand> locatorCommands = OsreoUtils.listLocatorCommands(osreoModel);
        return new ImageIntrospectorImpl(shells, locatorCommands, cmdAvailability);
    }

    public ImageIntrospectorSystem(List<Shell> shells, List<LocatorCommand> locatorCommands, ExecSiteProbeResults cmdAvailability) {
        super();
        this.shells = shells;
        this.locatorCommands = locatorCommands;
        this.cmdAvailability = cmdAvailability;
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

    public boolean canRunEntrypoint(String imageName, String entrypoint, List<String> commandOptions) {
        Entrypoint ep = new Entrypoint(imageName, entrypoint, commandOptions);
        Boolean result = imageToEntrypoints.get(imageName, ep);
        if (result == null) {
            try {
                result = ContainerUtils.canRunEntrypoint(imageName, entrypoint, commandOptions);
            } catch (ContainerFetchException | ContainerLaunchException e) {
                result = false;
            }
            imageToEntrypoints.put(entrypoint, ep, result);
            // Also declare the entrypoint as an available command
            // cmdAvailability.put(ep, entrypoint, result);
            cmdAvailability.put(entrypoint, ExecSites.docker(imageName), result);
        }
        return result;
    }

    public boolean hasCommand(String command) throws IOException, InterruptedException {
        ExecSite execSite = ExecSites.host();
        Boolean result = cmdAvailability.get(command, execSite);
        if (result == null) {
            String location = SysRuntimeImpl.forCurrentOs().which(command);
            result = location != null;
            cmdAvailability.put(command, execSite, result);
        }
        return result;
    }

    protected void findShell(ImageIntrospection result) {
        for (Shell shell : shells) {
            findShell(result, shell);
        }
    }

    protected void findShell(ImageIntrospection result, String shellName) {
        List<Shell> matches = shells.stream().filter(sh -> sh.getLabel().equals(shellName)).toList();
        for (Shell match : matches) {
            findShell(result, match);
        }
    }

    protected void findShell(ImageIntrospection result, Shell shell) {
        String shellName = shell.getLabel();
        List<String> commandOption = Arrays.asList(shell.getCommandOption());

        logger.info("Probing image [{}] for shell [{}]" , shellName);

        Set<String> shellProbeLocations = shell.getProbeLocations();
        for (String shellLocation : shellProbeLocations) {
            ShellSupport sh = result.getShellStatus().computeIfAbsent(shellName, key -> {
                return result.getModel().createResource().as(ShellSupport.class);
            });
            sh.setCommandPath(shellLocation);
            sh.setShellType(shell);
            sh.setCommandOption(shell.getCommandOption());

            // Check for the locator command
            String builtInLocator = shell.getLocatorCommand();
            if (builtInLocator != null) {
                sh.setLocatorCommand(builtInLocator); // TODO Verify?
            } else {
                for (LocatorCommand locatorCommand : locatorCommands) {
                    for (String locatorLocation : locatorCommand.getProbeLocations()) {
                        boolean hasCommand;
                        try {
                            hasCommand = hasCommand(locatorLocation);
                        } catch (IOException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        logger.info("Probe locator [{}] for shell [{}] with option [{}] using locator [{}]: {}", shellLocation, commandOption, locatorLocation, " found");

                        if (hasCommand) {
                            sh.setLocatorCommand(locatorLocation);
                        }
                    }
                }
            }
        }
    }
}
