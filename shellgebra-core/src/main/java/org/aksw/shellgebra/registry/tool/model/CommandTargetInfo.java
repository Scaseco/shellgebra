package org.aksw.shellgebra.registry.tool.model;

import java.util.Optional;
import java.util.stream.Stream;

public interface CommandTargetInfo {

    String getCommand();

    /** Whether the command may be run on the host. */
    Optional<Boolean> getAvailableOnHost();

    /** List docker images known for this command (regardless of availability). */
    Stream<String> getDockerImages();

    /** List docker images with known availability */
    default Stream<String> getAvailableImages() {
        // return imageToAvailability.entrySet().stream().filter(e -> Boolean.TRUE.equals(e.getValue())).map(Entry::getKey);
        return null;
    }

    default Optional<Boolean> getDockerImageAvailability(String imageName) {
        // return imageToAvailability.get(imageName);
        return null;
    }
}
