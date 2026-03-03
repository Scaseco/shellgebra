package org.aksw.vshell.registry;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.aksw.shellgebra.exec.resource.ReferenceCountedObject;

public class DynamicInputShared
    extends DynamicInputWrapper<DynamicInput>
{
    private ReferenceCountedObject<DynamicInput> source;
    private AtomicBoolean isClosed = new AtomicBoolean(false);

    protected DynamicInputShared(ReferenceCountedObject<DynamicInput> source) {
        super(null);
        this.source = source;
    }

    public static DynamicInputShared of(DynamicInput core) {
        ReferenceCountedObject<DynamicInput> source = ReferenceCountedObject.of(core);
        return new DynamicInputShared(source);
    }

    @Override
    protected DynamicInput getDelegate() {
        return source.get();
    }

    public DynamicInputShared dup() {
        source.acquire();
        source.get(); // Fail fast if dup failed.
        return new DynamicInputShared(source);

    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            source.release();
        } else {
            throw new IllegalStateException("Double close");
        }
    }

    @Override
    public String toString() {
        return "(DynamicInputShared " + (isClosed.get() ? "closed" : "open") + " (source: " + source + "))";
    }
}

//
//public DynamicInput acquire() {
//  source.acquire();
//  return new DynamicInputWrapper<>(null) {
//      private AtomicBoolean isClosed = new AtomicBoolean(false);
//
//      @Override
//      protected DynamicInput getDelegate() {
//          if (isClosed.get()) {
//              throw new IllegalStateException("Resource already closed");
//          }
//          return source.get();
//      }
//
//      @Override
//      public void close() throws IOException {
//          if (isClosed.compareAndSet(false, true)) {
//              source.release();
//          }
//      }
//  };
//}
