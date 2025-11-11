package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ProcessBuilderNative
    implements IProcessBuilder<ProcessBuilderNative>
{
    private ProcessBuilder delegate;

    public ProcessBuilderNative() {
        this(new ProcessBuilder());
    }

    public ProcessBuilderNative(ProcessBuilder delegate) {
        super();
        this.delegate = delegate;
    }

    private ProcessBuilder getDelegate() {
        return delegate;
    }

    @Override
    public List<String> command() {
        return getDelegate().command();
    }

    @Override
    public ProcessBuilderNative command(String... command) {
        getDelegate().command(command);
        return this;
    }

    @Override
    public ProcessBuilderNative command(List<String> command) {
        getDelegate().command(command);
        return this;
    }

    @Override
    public Path directory() {
        return getDelegate().directory().toPath();
    }

    @Override
    public ProcessBuilderNative directory(Path directory) {
        getDelegate().directory(directory.toFile());
        return this;
    }

    @Override
    public Map<String, String> environment() {
        return getDelegate().environment();
    }

    @Override
    public boolean redirectErrorStream() {
        return getDelegate().redirectErrorStream();
    }

    @Override
    public ProcessBuilderNative redirectErrorStream(boolean redirectErrorStream) {
        getDelegate().redirectErrorStream(redirectErrorStream);
        return this;
    }

    @Override
    public Process start() throws IOException {
        return getDelegate().start();
    }
}
