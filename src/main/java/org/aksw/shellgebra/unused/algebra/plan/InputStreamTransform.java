package org.aksw.shellgebra.unused.algebra.plan;

import java.io.InputStream;
import java.util.function.Function;

@FunctionalInterface
public interface InputStreamTransform
    extends Function<InputStream, InputStream>
{
    // InputStream apply(InputStream in); // throws IOException;
}
