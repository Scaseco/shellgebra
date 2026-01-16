package org.aksw.shellgebra.shim.cmd;

import org.aksw.commons.io.util.stream.InputStreamTransform;

public interface InputStreamTransformProvider {
    InputStreamTransform get(String name);
}
