package org.aksw.shellgebra.processbuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import org.aksw.shellgebra.exec.graph.JRedirect;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectJava;
import org.aksw.shellgebra.exec.graph.ProcessRunner;

/** A direct wrapper. */
public class ProcessBuilderCoreNativeWrapper
    extends ProcessBuilderBase<ProcessBuilderCoreNativeWrapper>
{
    private ProcessBuilder delegate;

    protected ProcessBuilderCoreNativeWrapper(ProcessBuilder delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public ProcessBuilderCoreNativeWrapper cloneActual() {
        // List<String> argv = getDelegate().command();
        ProcessBuilderCoreNativeWrapper result = new ProcessBuilderCoreNativeWrapper(new ProcessBuilder());
        return result;
    }

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
    public Process start(ProcessRunner executor) throws IOException {
        // TODO Auto-generated method stub
        return null;
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
}
