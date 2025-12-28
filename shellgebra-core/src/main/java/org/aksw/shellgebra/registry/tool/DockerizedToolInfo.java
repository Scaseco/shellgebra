package org.aksw.shellgebra.registry.tool;

/**
 * A concrete availability of a tool in a docker image (regardless of tag).
 */
public interface DockerizedToolInfo {
    String getImageName();

    /** Optional preferred tag of an image where the tool is guaranteed to work.
     *  If not set, 'latest' will be used. */
    String getPreferredImageTag();

    String getCommandName();
    String getCommandPath();
}
