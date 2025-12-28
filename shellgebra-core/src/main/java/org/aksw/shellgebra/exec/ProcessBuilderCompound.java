package org.aksw.shellgebra.exec;

import java.util.ArrayList;
import java.util.List;

public abstract class ProcessBuilderCompound<X extends ProcessBuilderCompound<X>>
    extends ProcessBuilderCore<X>
{
    private List<? extends IProcessBuilderCore<?>> processBuilders;

    public ProcessBuilderCompound() {
        super();
        this.processBuilders = List.of();
    }

    // XXX Probably we want a method that creates a final context based on this builder's state and the process runner.
    protected List<? extends IProcessBuilderCore<?>> copyProcessBuilders() {
        return new ArrayList<>(processBuilders);
    }

    public List<? extends IProcessBuilderCore<?>> processBuilders() {
        return processBuilders;
    }

    public X processBuilders(IProcessBuilderCore<?>... processBuilders) {
        processBuilders(List.of(processBuilders));
        return self();
    }

    public X processBuilders(List<? extends IProcessBuilderCore<?>> processBuilders) {
        this.processBuilders = List.copyOf(processBuilders);
        return self();
    }

    @Override
    public X clone() {
        X result = super.clone();
        result.processBuilders(processBuilders());
        return result;
    }
}
