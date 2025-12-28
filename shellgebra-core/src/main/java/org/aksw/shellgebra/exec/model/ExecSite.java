package org.aksw.shellgebra.exec.model;

/**
 * An execution site is a reference for where something can be run, such as
 * in Java, in a Docker Image or on the Host.
 */
public interface ExecSite {
    // ExecSite does create Stages because e.g. it should not be tied to a
    // FileMapper that would be needed for a DockerStage.
    // Stage newStage(CmdOp op);)
    <T> T accept(ExecSiteVisitor<T> visitor);
}
