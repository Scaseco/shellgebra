package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.aksw.shellgebra.exec.graph.JRedirect;
import org.aksw.shellgebra.exec.graph.ProcessRunner;

// TODO Remove this wrapper class in favor of the non-wrapper version.
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

    @Override
    public ProcessBuilderNative clone() {
        throw new RuntimeException("not implemented yet");
//    	return new ProcessBuilderNative(
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

//    @Override
//    public void bind(Path hostPath, String containerPath, boolean write) {
//        // XXX Add flag that enables tracking for debugging.
//    }

    @Override
    public Process start(ProcessRunner executor) throws IOException {
//        return executor.start(delegate);
        return null;
    }

    @Override
    public ProcessBuilderNative redirectInput(JRedirect redirect) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JRedirect redirectInput() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ProcessBuilderNative redirectOutput(JRedirect redirect) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JRedirect redirectOutput() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ProcessBuilderNative redirectError(JRedirect redirect) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JRedirect redirectError() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean supportsAnonPipeRead() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean supportsAnonPipeWrite() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean supportsDirectNamedPipe() {
        // TODO Auto-generated method stub
        return false;
    }
}
