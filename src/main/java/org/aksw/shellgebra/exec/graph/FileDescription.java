package org.aksw.shellgebra.exec.graph;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileDescription<T>
    implements AutoCloseable
{
    private ReferenceCountedObject<T> resource;
    private AtomicBoolean isClosed = new AtomicBoolean();

    private FileDescription(ReferenceCountedObject<T> resource) {
        super();
        this.resource = resource;
    }

    public boolean isOpen() {
        return resource.isOpen();
    }

    public T get() {
        return resource.get();
    }

    /**
     * Duplicate the file description.
     * Will try to acquire the underlying resource.
     * The resulting FileDescription may be "born dead" if the resource is already closed.
     * Check the state with {@link FileDescription#isOpen()}.
     */
    public FileDescription<T> dup() {
        resource.acquire();
        return new FileDescription<>(resource);
    }

    public static <T extends AutoCloseable> FileDescription<T> auto(T obj) {
        ReferenceCountedObject<T> resource = ReferenceCountedObject.of(obj);
        return of(resource);
    }

    public static <T> FileDescription<T> of(ReferenceCountedObject<T> resource) {
        return new FileDescription<>(resource);
    }

    public static <T> FileDescription<FdResource> of(OutputStream os) {
        return auto(FdResource.of(os));
    }

    public static <T> FileDescription<FdResource> of(InputStream is) {
        return auto(FdResource.of(is));
    }

    public static <T> FileDescription<FdResource> of(Path path) {
        return auto(FdResource.of(path));
    }

    @Override
    public void close() {
        if (isClosed.compareAndSet(false, true)) {
            resource.release();
        }
    }
}
