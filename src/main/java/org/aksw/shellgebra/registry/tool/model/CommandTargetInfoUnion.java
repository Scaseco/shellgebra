package org.aksw.shellgebra.registry.tool.model;

import java.util.Optional;
import java.util.stream.Stream;

public class CommandTargetInfoUnion
    implements CommandTargetInfo
{
    protected String command;
    protected ToolInfoUnion toolInfoUnion;

    protected Stream<CommandTargetInfo> getTargetInfos() {
        return toolInfoUnion.streamToolInfos()
            .map(x -> x.getCommand(command))
            .flatMap(Optional::stream);
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public Optional<Boolean> getAvailableOnHost() {
        return getTargetInfos()
            .map(CommandTargetInfo::getAvailableOnHost)
            .flatMap(Optional::stream)
            .findFirst();
    }

    @Override
    public Stream<String> getDockerImages() {
        return getTargetInfos().flatMap(CommandTargetInfo::getAvailableImages).distinct();
    }

    @Override
    public Optional<Boolean> getDockerImageAvailability(String imageName) {
        return getTargetInfos()
            .map(x -> x.getDockerImageAvailability(imageName))
            .flatMap(Optional::stream)
            .findFirst();
    }
}
