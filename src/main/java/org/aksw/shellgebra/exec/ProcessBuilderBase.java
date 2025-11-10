package org.aksw.shellgebra.exec;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public abstract class ProcessBuilderBase<X extends ProcessBuilderBase<X>>
    implements IProcessBuilder<X>
{
    private List<String> command;
    private Path directory;
    private Map<String,String> environment;
    private boolean redirectErrorStream;
    // private Redirect[] redirects;

    @SuppressWarnings("unchecked")
    protected X self() {
        return (X)this;
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

    @Override
    public Path directory() {
        return directory;
    }

    @Override
    public X directory(Path directory) {
        this.directory = directory;
        return self();
    }

    @Override
    public Map<String, String> environment() {
        return environment;
    }

    @Override
    public boolean redirectErrorStream() {
        return redirectErrorStream;
    }

    @Override
    public X redirectErrorStream(boolean redirectErrorStream) {
        this.redirectErrorStream = redirectErrorStream;
        return self();
    }
}
