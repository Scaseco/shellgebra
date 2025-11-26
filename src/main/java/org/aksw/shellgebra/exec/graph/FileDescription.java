package org.aksw.shellgebra.exec.graph;

public interface FileDescription<T>
    extends AutoCloseable {

    boolean isOpen();

    /** Get the raw reference to the resource that may be closed. */
    T getRaw();
    T get();
    FileDescription<T> dup();

    @Override
    public void close();

    /** Dup followed by a check for whether the resource is alive. */
    default FileDescription<T> checkedDup() {
        FileDescription<T> result = dup();
        if (!result.isOpen()) {
            throw new IllegalStateException("Resource already closed: " + getRaw());
        }
        return result;
    }

    public static <T extends AutoCloseable> FileDescription<T> auto(T obj) {
        ReferenceCountedObject<T> resource = ReferenceCountedObject.of(obj);
        return of(resource);
    }

    public static <T> FileDescription<T> of(ReferenceCountedObject<T> resource) {
        return new FileDescriptionImpl<>(resource);
    }
}
