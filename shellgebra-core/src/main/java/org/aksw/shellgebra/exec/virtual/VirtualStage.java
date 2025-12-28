package org.aksw.shellgebra.exec.virtual;

import org.aksw.shellgebra.exec.Stage;

/**
 * Marker interface for stages that are not concrete.
 * Virtual stages need to be resolved into concrete ones
 * before they can executed.
 */
public interface VirtualStage
    extends Stage
{
}
