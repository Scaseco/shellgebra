package org.aksw.shellgebra.exec.graph;

import java.util.function.Supplier;

import org.aksw.shellgebra.exec.IProcessBuilder;

public interface ProcessBuilderFactory<X extends IProcessBuilder<X>>
    extends Supplier<X>
{
}
