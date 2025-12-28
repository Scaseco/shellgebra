package org.aksw.shellgebra.model.pipeline;

import java.nio.file.Path;

public class ContainerEnvironment {
    /** The container that can be shared with other containers. */
    private Path sharedFolder;

    public ContainerEnvironment(Path sharedFolder) {
        this.sharedFolder = sharedFolder;
    }

    public Path getSharedFolder() {
        return sharedFolder;
    }
}
