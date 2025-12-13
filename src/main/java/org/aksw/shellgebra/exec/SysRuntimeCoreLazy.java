package org.aksw.shellgebra.exec;

import java.util.Objects;
import java.util.function.Supplier;

/** SysRuntimeCore implementation that initializes the backend when {@link #getDelegate()} is called. */
public class SysRuntimeCoreLazy
    extends SysRuntimeWrapperBase<SysRuntime>
{
    private Supplier<SysRuntime> factory;
    private SysRuntime delegate = null;
    private boolean isOpen = true;

    public SysRuntimeCoreLazy(Supplier<SysRuntime> factory) {
        super(null);
        this.factory = factory;
    }

    public static SysRuntime of(Supplier<SysRuntime> factory) {
        return new SysRuntimeCoreLazy(factory);
    }

    @Override
    public SysRuntime getDelegate() {
        if (!isOpen) {
            throw new RuntimeException("Closed");
        }
        if (delegate == null) {
            synchronized (this) {
                if (delegate == null) {
                    if (isOpen) {
                        delegate = factory.get();
                        Objects.requireNonNull(delegate, "Factory supplied null");
                    }
                }
            }
        }
        return delegate;
    }

    @Override
    public void close() {
        isOpen = false;
        if (delegate != null) {
            synchronized (this) {
                if (delegate != null) {
                    try {
                        delegate.close();
                    } finally {
                        delegate = null;
                    }
                }
            }
        }
    }
}
