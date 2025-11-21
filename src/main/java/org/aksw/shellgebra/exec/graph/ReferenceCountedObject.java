package org.aksw.shellgebra.exec.graph;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

//Resource<T> from previous response, repeated for clarity
// XXX Could reuse https://github.com/almson/almson-refcount
public final class ReferenceCountedObject<T> {
    private final T resource;
    private final AutoCloseable closeAction;
    private final AtomicInteger refCount = new AtomicInteger(1);

    private ReferenceCountedObject(T resource, AutoCloseable closeAction) {
        this.resource = Objects.requireNonNull(resource);
        this.closeAction = Objects.requireNonNull(closeAction);
    }

    public T get() {
        if (refCount.get() <= 0) {
            throw new IllegalStateException("Resource already released");
        }
        return resource;
    }

    public ReferenceCountedObject<T> acquire() {
        // Never increment once zero was reached.
        refCount.getAndUpdate(old -> old > 0 ? old + 1 : 0);
        return this;
    }

    public void release() {
        int oldCount = refCount.decrementAndGet();
        if (oldCount < 0) {
            throw new IllegalStateException("Resource reference count negative — double close?");
        }
        if (oldCount == 0) {
            try {
                closeAction.close();
            } catch (Exception e) {
                Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            }
        }
    }

    public boolean isOpen() {
        return refCount.get() > 0;
    }

    /** For debugging. */
    public int refCount() {
        return refCount.get();
    }

    public static <T extends AutoCloseable> ReferenceCountedObject<T> of(T obj) {
        return of(obj, obj);
    }

    public static <T> ReferenceCountedObject<T> of(T obj, AutoCloseable closeAction) {
        return new ReferenceCountedObject<>(obj, closeAction);
    }
}
