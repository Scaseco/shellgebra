package org.aksw.shellgebra.shim.cmd;

import org.aksw.commons.io.util.stream.OutputStreamTransform;

public interface OutputStreamTransformProvider {
    OutputStreamTransform get(String name);
}
