package org.aksw.shellgebra.registry.tool.model;

import java.util.Optional;
import java.util.stream.Stream;

public interface ToolInfo {
    String getName();

    /** Known images which do NOT contain the tool - i.e. search is known to not yield a result. */
    Stream<String> getAbsenceInDockerImages();

    default boolean isAbsentInDockerImage(String dockerImage) {
        boolean result = getAbsenceInDockerImages().noneMatch(item -> item.equals(dockerImage));
        return result;
    }

    Optional<Boolean> getAbsentOnHost();

    CommandTargetInfo findCommandByImage(String imageName);
    CommandTargetInfo findCommandOnHost();

    // Map<String, CommandTargetInfo> getCommandsByPath();
    Stream<CommandTargetInfo> list();

    Optional<CommandTargetInfo> getCommand(String commandPath);
}
