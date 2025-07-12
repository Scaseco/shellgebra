package org.aksw.shellgebra.registry.codec;

import org.aksw.shellgebra.unused.algebra.plan.OutputStreamTransform;

public interface OutputStreamTransformProvider {
    OutputStreamTransform get(String name);
}
