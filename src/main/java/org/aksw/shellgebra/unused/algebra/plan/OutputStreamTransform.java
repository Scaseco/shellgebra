package org.aksw.shellgebra.unused.algebra.plan;

import java.io.OutputStream;
import java.util.function.Function;

@FunctionalInterface
public interface OutputStreamTransform
    extends Function<OutputStream, OutputStream>
{
}
