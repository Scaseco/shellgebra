package org.aksw.shellgebra.processbuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.aksw.shellgebra.exec.IProcessBuilder;
import org.aksw.shellgebra.exec.graph.JRedirect;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectJava;
import org.aksw.shellgebra.exec.graph.ProcessRunner;

/**
 * A stateless {@link IProcessBuilder} adapter for Java's native {@link ProcessBuilder}.
 */
public class ProcessBuilderCoreNativeWrapper
    implements IProcessBuilder<ProcessBuilderCoreNativeWrapper>
{
    private ProcessBuilder delegate;

    protected ProcessBuilderCoreNativeWrapper(ProcessBuilder delegate) {
        super();
        this.delegate = delegate;
    }

    public static ProcessBuilder clone(ProcessBuilder original) {
        ProcessBuilder clone = new ProcessBuilder();
        applySettings(clone, original);
        return clone;
    }

    public static ProcessBuilder applySettings(ProcessBuilder clone, ProcessBuilder original) {
        clone.command(original.command());
        clone.environment().putAll(original.environment());
        clone.redirectInput(original.redirectInput());
        clone.redirectOutput(original.redirectOutput());
        clone.redirectError(original.redirectError());
        clone.directory(original.directory());
        return clone;
    }

    @Override
    public ProcessBuilderCoreNativeWrapper clone() {
        ProcessBuilder copy = clone(delegate);
        return new ProcessBuilderCoreNativeWrapper(copy);
    }
//    @Override
//    public ProcessBuilderCoreNativeWrapper cloneActual() {
//        // List<String> argv = getDelegate().command();
//        ProcessBuilderCoreNativeWrapper result = new ProcessBuilderCoreNativeWrapper(new ProcessBuilder());
//        return result;
//    }

    public static ProcessBuilderCoreNativeWrapper wrap(ProcessBuilder delegate) {
        return new ProcessBuilderCoreNativeWrapper(delegate);
    }

    public ProcessBuilder getDelegate() {
        return delegate;
    }

    @Override
    public Path directory() {
        return Optional.ofNullable(getDelegate().directory()).map(File::toPath).orElse(null);
    }

    @Override
    public ProcessBuilderCoreNativeWrapper directory(Path directory) {
        getDelegate().directory(directory.toFile());
        return this;
    }

    @Override
    public Map<String, String> environment() {
        return getDelegate().environment();
    }

    @Override
    public boolean redirectErrorStream() {
        return false;
    }

    @Override
    public ProcessBuilderCoreNativeWrapper redirectErrorStream(boolean redirectErrorStream) {
        getDelegate().redirectErrorStream(redirectErrorStream);
        return this;
    }

    @Override
    public Process start() throws IOException {
        return getDelegate().start();
    }

    @Override
    public Process start(ProcessRunner context) throws IOException {
        throw new UnsupportedOperationException("This low-level wrapper does not support being launched with a context.");
    }

    @Override
    public ProcessBuilderCoreNativeWrapper redirectInput(JRedirect redirect) {
        if (redirect instanceof JRedirectJava r) {
            getDelegate().redirectInput(r.redirect());
        } else {
            throw new IllegalArgumentException("Unsupported redirect: " + redirect);
        }
        return this;
    }

    @Override
    public JRedirect redirectInput() {
        return new JRedirectJava(getDelegate().redirectInput());
    }

    @Override
    public ProcessBuilderCoreNativeWrapper redirectOutput(JRedirect redirect) {
        if (redirect instanceof JRedirectJava r) {
            getDelegate().redirectOutput(r.redirect());
        } else {
            throw new IllegalArgumentException("Unsupported redirect: " + redirect);
        }
        return this;
    }

    @Override
    public JRedirect redirectOutput() {
        return new JRedirectJava(getDelegate().redirectOutput());
    }

    @Override
    public ProcessBuilderCoreNativeWrapper redirectError(JRedirect redirect) {
        if (redirect instanceof JRedirectJava r) {
            getDelegate().redirectError(r.redirect());
        } else {
            throw new IllegalArgumentException("Unsupported redirect: " + redirect);
        }
        return this;
    }

    @Override
    public JRedirect redirectError() {
        return new JRedirectJava(getDelegate().redirectError());
    }

    @Override
    public ProcessBuilderCoreNativeWrapper command(String... command) {
        getDelegate().command(command);
        return this;
    }

    @Override
    public ProcessBuilderCoreNativeWrapper command(List<String> command) {
        getDelegate().command(command);
        return this;
    }

    @Override
    public boolean supportsAnonPipeRead() {
        return true;
    }

    @Override
    public boolean supportsAnonPipeWrite() {
        return true;
    }

    @Override
    public boolean supportsDirectNamedPipe() {
        return true;
    }

    @Override
    public boolean accessesStdIn() {
        return true;
    }

    @Override
    public List<String> command() {
        return getDelegate().command();
    }
}
