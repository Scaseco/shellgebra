package org.aksw.shellgebra.registry.codec;

import org.aksw.commons.io.util.stream.OutputStreamTransform;

public interface OutputStreamTransformProvider {
    OutputStreamTransform get(String name);
}
