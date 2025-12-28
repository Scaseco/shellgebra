package org.aksw.vshell.registry;

import java.io.InputStream;
import java.io.OutputStream;

public class ProcessWrapper<T extends Process>
    extends Process
{
    private T delegate;

    public ProcessWrapper(T delegate) {
        super();
        this.delegate = delegate;
    }

    public T getDelegate() {
        return delegate;
    }

    @Override
    public OutputStream getOutputStream() {
        return getDelegate().getOutputStream();
    }

    @Override
    public InputStream getInputStream() {
        return getDelegate().getInputStream();
    }

    @Override
    public InputStream getErrorStream() {
        return getDelegate().getErrorStream();
    }

    @Override
    public int waitFor() throws InterruptedException {
        return getDelegate().waitFor();
    }

    @Override
    public int exitValue() {
        return getDelegate().exitValue();
    }

    @Override
    public void destroy() {
        getDelegate().destroy();
    }
}
