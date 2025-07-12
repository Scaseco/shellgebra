package org.aksw.shellgebra.registry.codec;

import org.aksw.shellgebra.unused.algebra.plan.InputStreamTransform;

public interface InputStreamTransformProvider {
    InputStreamTransform get(String name);
}
