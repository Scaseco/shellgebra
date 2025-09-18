package org.aksw.shellgebra.registry.tool.model;

import java.util.Optional;
import java.util.stream.Stream;

public interface ToolInfo {
    String getName();
    Stream<String> getAbsenceInDockerImages();

    default boolean isAbsentInDockerImage(String dockerImage) {
        boolean result = getAbsenceInDockerImages().contains(dockerImage);
        return result;
    }

    Optional<Boolean> getAbsentOnHost();

    CommandTargetInfo findCommandByImage(String imageName);
    CommandTargetInfo findCommandOnHost();

    // Map<String, CommandTargetInfo> getCommandsByPath();
    Stream<CommandTargetInfo> list();

    Optional<CommandTargetInfo> getCommand(String commandPath);
}
