package org.aksw.shellgebra.exec.graph;

public class FileDescriptionWrapper<T>
    implements FileDescription<T>
{
    private FileDescription<T> delegate;

    public FileDescriptionWrapper(FileDescription<T> delegate) {
        super();
        this.delegate = delegate;
    }

    public FileDescription<T> getDelegate() {
        return delegate;
    }

    @Override
    public boolean isOpen() {
        return getDelegate().isOpen();
    }

    @Override
    public T getRaw() {
        return getDelegate().getRaw();
    }

    @Override
    public T get() {
        return getDelegate().get();
    }

    @Override
    public FileDescription<T> dup() {
        return getDelegate().dup();
    }

    @Override
    public void close() {
        getDelegate().close();
    }
}
