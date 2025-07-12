package org.aksw.shellgebra.registry.codec;

import org.aksw.shellgebra.unused.algebra.plan.InputStreamTransform;
import org.aksw.shellgebra.unused.algebra.plan.OutputStreamTransform;

public record JavaStreamTransform(
    InputStreamTransform inputStreamTransform,
    OutputStreamTransform outputStreamTransform)
{
}
