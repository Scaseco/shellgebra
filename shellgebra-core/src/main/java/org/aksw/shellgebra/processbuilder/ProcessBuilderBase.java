package org.aksw.shellgebra.processbuilder;

import java.util.List;

import org.aksw.shellgebra.exec.IProcessBuilder;

public abstract class ProcessBuilderBase<X extends ProcessBuilderBase<X>>
    extends ProcessBuilderCore<X>
    implements IProcessBuilder<X>
{
    private List<String> command;

    public ProcessBuilderBase() {
        super();
    }

    @Override
    public List<String> command() {
        return command;
    }

    @Override
    public X command(String... command) {
        command(List.of(command));
        return self();
    }

    @Override
    public X command(List<String> command) {
        this.command = List.copyOf(command);
        return self();
    }

    protected void applySettings(ProcessBuilderBase<?> target) {
        target.applySettings(this);
    }

    @Override
    public X clone() {
        X result = super.clone();
        result.command(command());
        return result;
    }
}
