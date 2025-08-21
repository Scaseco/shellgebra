package org.aksw.shellgebra.algebra.stream.op;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.aksw.commons.util.docker.ContainerUtils;
import org.aksw.commons.util.docker.ImageIntrospector;
import org.aksw.commons.util.docker.ImageIntrospectorImpl;
import org.aksw.jenax.model.osreo.ImageIntrospection;
import org.aksw.jenax.model.osreo.ShellSupport;
import org.aksw.shellgebra.algebra.stream.transformer.StreamOpTransformer;
import org.aksw.shellgebra.exec.SysRuntimeImpl;
import org.aksw.shellgebra.registry.tool.CommandTargetInfo;
import org.aksw.shellgebra.registry.tool.ToolInfo;
import org.aksw.shellgebra.registry.tool.ToolInfoProviderImpl;
import org.aksw.shellgebra.registry.tool.ToolRegistry;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToolUsage {
    private static final Logger logger = LoggerFactory.getLogger(ToolUsage.class);

    public static ToolInfoProviderImpl analyzeUsage(StreamOp streamOp) {
        StreamOpTransformToolUsage toolUsage = new StreamOpTransformToolUsage();
        StreamOpTransformer.transform(streamOp, toolUsage);
        ToolInfoProviderImpl tools = toolUsage.getTools();

        // TODO Inject custom images, e.g. adfreiburg/qlever:commit-f59763c
        return tools;
    }

    /**
     * Given a set of tools with verified commands and images, check for whether
     * some tools are provided by other containers.
     *
     * @param tools
     */
    public static void enrich(ToolInfoProviderImpl tools, String ... targetImages) {
        Model model = RDFDataMgr.loadModel("shell-ontology.ttl");
        ImageIntrospector imageIntrospector = ImageIntrospectorImpl.of(model);

        List<String> toolNames = tools.list().stream().map(ToolInfo::getName).toList();
        List<String> derivedImageNames = tools.list().stream().flatMap(tool -> tool.getCommandsByPath().values().stream())
            .flatMap(cmd -> cmd.getDockerImages().stream()).collect(Collectors.toList());

        List<String> imageNames = new ArrayList<>();
        imageNames.addAll(Arrays.asList(targetImages));
        imageNames.addAll(derivedImageNames);

        ToolInfoProviderImpl enrichedRegistry = new ToolInfoProviderImpl();
        // Map<String, ToolInfo> toolInfo = new LinkedHashMap<>();


//        for (ToolInfo toolInfo : toolInfos) {
//        	toolInfo.get
//        	toolInfo.getCommandsByPath()
//        }

        // ToolRegistry baseRegistry = ToolRegistry.get();

        for (String toolName : toolNames) {
            analyzeToolAvailability(imageIntrospector, imageNames, enrichedRegistry, toolName);
        }

        System.out.println(enrichedRegistry);
    }

    public static void analyzeToolAvailability(ImageIntrospector imageIntrospector, List<String> imageNames,
            ToolInfoProviderImpl enrichedRegistry, String toolName) {
        // ToolInfo baseToolInfo = baseRegistry.getToolInfo(toolName).orElseGet(() -> new ToolInfo(toolName));
        // ToolInfo toolInfo = enrichedRegistry.merge(baseToolInfo);
        ToolInfo toolInfo = enrichedRegistry.getOrCreate(toolName);

        for (String imageName : imageNames) {

            if (toolInfo.getAbsentOnHost() != null) {
                String hostCmd = null;
                try {
                    hostCmd = SysRuntimeImpl.forCurrentOs().which(toolName);
                } catch (IOException | InterruptedException e) {
                }

                // boolean isAvailabeOnHost = hostCmd != null;
                if (hostCmd != null) {
                    toolInfo.getOrCreateCommand(hostCmd).setAvailableOnHost(true);
                    toolInfo.setAbsentOnHost(false);
                } else {
                    toolInfo.setAbsentOnHost(true);
                }
            }

            // Only scan image if the tool is not declared to be absent.
            boolean isAbsent = toolInfo.isAbsentInDockerImage(imageName);
            if (!isAbsent) {
                CommandTargetInfo imageCmd = toolInfo.findCommandByImage(imageName);
                Boolean availability = imageCmd == null ? null : imageCmd.getDockerImageAvailability(imageName);

                if (availability == null) {
                    try {
                        ImageIntrospection introspection = imageIntrospector.inspect(imageName, true);
                        Map<String, ShellSupport> shells = introspection.getShellStatus();
                        for (ShellSupport shellSupport : shells.values()) {
                            String entryPoint = shellSupport.getCommandPath();
                            String locatorCommand = shellSupport.getLocatorCommand();
                            String cmd = ContainerUtils.checkImage(imageName, entryPoint, locatorCommand, toolName);

                            // Boolean availability = toolInfo.get

                            // String cmd = ContainerUtils.checkImageForCommand(imageName, toolName);
                            if (cmd != null) {
                                toolInfo.getOrCreateCommand(cmd).addDockerImageAvailability(imageName);
                                // Once the command is located (regardless of the shell) we are finished here.
                                break;
                            }
                        }
                    } catch (Exception e) {
                        logger.info("Absence: " + toolName + " in " + imageName);
                        toolInfo.setAbsentInDockerImage(toolName);
                    }
                }
            }
        }
    }
}
