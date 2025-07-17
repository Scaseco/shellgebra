package org.aksw.shellgebra.algebra.stream.op;

import java.io.IOException;
import java.util.List;

import org.aksw.commons.util.docker.ContainerUtils;
import org.aksw.commons.util.docker.ImageIntrospector;
import org.aksw.commons.util.docker.ImageIntrospectorImpl;
import org.aksw.shellgebra.exec.SysRuntimeImpl;
import org.aksw.shellgebra.registry.tool.CommandPathInfo;
import org.aksw.shellgebra.registry.tool.ToolInfo;
import org.aksw.shellgebra.registry.tool.ToolInfoProviderImpl;
import org.aksw.shellgebra.registry.tool.ToolRegistry;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToolUsage {
    private static final Logger logger = LoggerFactory.getLogger(ToolUsage.class);


    /**
     * Given a set of tools which verified commands and images, check for whether
     * some tools are provided by other containers.
     *
     * @param tools
     */
    public static void enrich(ToolInfoProviderImpl tools) {
        Model model = RDFDataMgr.loadModel("shell-ontology.ttl");
        ImageIntrospector imageIntrospector = ImageIntrospectorImpl.of(model);

        List<String> toolNames = tools.list().stream().map(ToolInfo::getName).toList();
        List<String> imageNames = tools.list().stream().flatMap(tool -> tool.getCommandsByPath().values().stream())
            .flatMap(cmd -> cmd.getDockerImages().stream()).toList();

        ToolInfoProviderImpl enrichedRegistry = new ToolInfoProviderImpl();
        // Map<String, ToolInfo> toolInfo = new LinkedHashMap<>();


//        for (ToolInfo toolInfo : toolInfos) {
//        	toolInfo.get
//        	toolInfo.getCommandsByPath()
//        }

        ToolRegistry baseRegistry = ToolRegistry.get();

        for (String toolName : toolNames) {
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

                boolean isAbsent = toolInfo.isAbsentInDockerImage(imageName);
                if (!isAbsent) {
                    CommandPathInfo imageCmd = toolInfo.findCommandByImage(imageName);
                    Boolean availability = imageCmd == null ? null : imageCmd.getDockerImageAvailability(imageName);

                    if (availability == null) {
                        try {
                            // ImageIntrospection introspection = imageIntrospector.inspect(imageName, true);
                            // introspection.getShellStatus();

                            // Boolean availability = toolInfo.get

                            String cmd = ContainerUtils.checkImageForCommand(imageName, toolName);
                            if (cmd != null) {
                                toolInfo.getOrCreateCommand(cmd).addDockerImageAvailability(imageName);
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
}
