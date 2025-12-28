package org.aksw.shellgebra.exec.graph;

import java.util.concurrent.atomic.AtomicBoolean;

public class FileDescriptionImpl<T>
    implements FileDescription<T>
{
    private ReferenceCountedObject<T> resource;
    private AtomicBoolean isClosed = new AtomicBoolean();

    public FileDescriptionImpl(ReferenceCountedObject<T> resource) {
        super();
        this.resource = resource;
    }

    @Override
    public boolean isOpen() {
        return resource.isOpen();
    }

    @Override
    public T get() {
        return resource.get();
    }

    @Override
    public T getRaw() {
        return resource.getRaw();
    }

    /**
     * Duplicate the file description.
     * Will try to acquire the underlying resource.
     * The resulting FileDescription may be "born dead" if the resource is already closed.
     * Check the state with {@link FileDescription#isOpen()}.
     */
    @Override
    public FileDescription<T> dup() {
        resource.acquire();
        return new FileDescriptionImpl<>(resource);
    }

    @Override
    public void close() {
        if (isClosed.compareAndSet(false, true)) {
            resource.release();
        }
    }
}
