package org.aksw.shellgebra.registry.codec;

import org.aksw.commons.io.util.stream.InputStreamTransform;

public interface InputStreamTransformProvider {
    InputStreamTransform get(String name);
}
