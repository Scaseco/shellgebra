package org.aksw.shellgebra.registry.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.aksw.shellgebra.registry.codec.CodecRegistry;
import org.aksw.shellgebra.registry.tool.model.ToolInfoProvider;

/**
 * Registry to resolve tools to commands.
 * Commands may be executable on the host or using docker containers.
 */
public class ToolRegistry {
    protected List<ToolInfoProvider> toolInfoProviders = new ArrayList<>();

    private static ToolRegistry defaultRegistry = null;

    public static ToolRegistry get() {
        if (defaultRegistry == null) {
            synchronized (CodecRegistry.class) {
                if (defaultRegistry == null) {
                    defaultRegistry = new ToolRegistry();
                    loadDefaults(defaultRegistry);
                }
            }
        }
        return defaultRegistry;
    }


    record ToolObservation(String toolName, String platform, String command, String dockerImage) {}

    public static void loadDefaults(ToolRegistry registry) {
        ToolInfoProviderImpl toolProvider = new ToolInfoProviderImpl();

        toolProvider.getOrCreate("lbzip2")
            .getOrCreateCommand("/usr/bin/lbzip2")
                .addAvailabilityDockerImage("nestio/lbzip2");

        toolProvider.getOrCreate("gzip")
            .getOrCreateCommand("/usr/bin/gzip")
                .setAvailabilityHost(null)
                .setDockerImageAvailability("foo", null)
                .setDockerImageAvailability("bar", null);

        toolProvider.getOrCreate("bzip2")
            .getOrCreateCommand("/usr/bin/bzip2");

        toolProvider.getOrCreate("cat")
            .getOrCreateCommand("/usr/bin/cat");

        toolProvider.getOrCreate("rapper")
            .getOrCreateCommand("/usr/bin/rapper");

        /*
         How to best separate candidate model from the availability assessment?
         Could have an assment model with maps
         Table<ExecSite, Command, Boolean> availabilities.
         Command would have to be mapped back to the tool selector it corresponds to.
         Tool selector: name + version - but version might be unknown.

         */

        /*
        ToolInfoProvider toolProvider = ToolInfoProviderImpl.newBuilder()

            .add(ToolInfo.newBuilder()
                .setName("lbzip2")
                .addCommand(CommandPathInfo.newBuilder()
                    .setCommand("/usr/bin/lbzip2")
                    .addDockerImageName("nestio/lbzip2")
                    .build())
                .build())

            .add(ToolInfo.newBuilder()
                .setName("gzip")
                .addCommand(CommandPathInfo.newBuilder()
                    .setCommand("/usr/bin/gzip")
                    .build())
                .build())

            .add(ToolInfo.newBuilder()
                .setName("bzip2")
                .addCommand(CommandPathInfo.newBuilder()
                    .setCommand("/usr/bin/bzip2")
                    .build())
                .build())

            .add(ToolInfo.newBuilder()
                .setName("cat")
                .addCommand(CommandPathInfo.newBuilder()
                    .setCommand("/usr/bin/cat")
                    .build())
                .build())

            .add(ToolInfo.newBuilder()
                .setName("rapper")
                .addCommand(CommandPathInfo.newBuilder()
                    .setCommand("/usr/bin/rapper")
                    // .addDockerImageName("nestio/lbzip2")
                    .build())
                .build())

            .build()
            ;
        */

        registry.addToolInfoProvider(toolProvider);
    }

//    public List<DockerizedToolInfo> getDockerImages(String toolName) {
//        List<DockerizedToolInfo> result = imageProviders.stream()
//            .flatMap(provider -> provider.get(toolName).stream())
//            .toList();
//        return result;
//    }

    public Optional<ToolInfoImpl> getToolInfo(String name) {
        Optional<ToolInfoImpl> result = toolInfoProviders.stream()
            .map(provider -> provider.get(name))
            .flatMap(Optional::stream)
            .findFirst();
        return result;
    }

    public ToolRegistry addToolInfoProvider(ToolInfoProvider provider) {
        toolInfoProviders.add(0, provider);
        return this;
    }
}
