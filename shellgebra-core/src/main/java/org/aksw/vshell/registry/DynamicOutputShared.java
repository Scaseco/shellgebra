package org.aksw.vshell.registry;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.aksw.shellgebra.exec.resource.ReferenceCountedObject;

public class DynamicOutputShared
    extends DynamicOutputWrapper<DynamicOutput>
{
    private ReferenceCountedObject<DynamicOutput> sink;
    private AtomicBoolean isClosed = new AtomicBoolean(false);

    protected DynamicOutputShared(ReferenceCountedObject<DynamicOutput> sink) {
        super(null);
        this.sink = sink;
    }

    public static DynamicOutputShared of(DynamicOutput core) {
        ReferenceCountedObject<DynamicOutput> sink = ReferenceCountedObject.of(core);
        return new DynamicOutputShared(sink);
    }

    @Override
    protected DynamicOutput getDelegate() {
        return sink.get();
    }

    public DynamicOutputShared dup() {
        sink.acquire();
        sink.get(); // Fail fast if already closed.
        return new DynamicOutputShared(sink);
    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            sink.release();
        }
    }

    @Override
    public String toString() {
        return "(DynamicInputShared " + (isClosed.get() ? "closed" : "open") + " (sink: " + sink + "))";
    }
}
