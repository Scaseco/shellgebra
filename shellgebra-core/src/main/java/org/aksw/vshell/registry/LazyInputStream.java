package org.aksw.vshell.registry;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.Callable;

import org.apache.commons.io.input.ProxyInputStream;

public class LazyInputStream
    extends ProxyInputStream
{
    private Callable<InputStream> creator;

    protected LazyInputStream(Callable<InputStream> creator) {
        super((InputStream)null);
        this.creator = Objects.requireNonNull(creator);
    }

    public static LazyInputStream of(Callable<InputStream> creator) {
        return new LazyInputStream(creator);
    }

    @Override
    public int available() throws IOException {
        ensureDelegate();
        return super.available();
    }

    @Override
    public synchronized void mark(int readLimit) {
        try {
            ensureDelegate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        super.mark(readLimit);
    }

    @Override
    public boolean markSupported() {
        try {
            ensureDelegate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return super.markSupported();
    }

    @Override
    public synchronized void reset() throws IOException {
        ensureDelegate();
        super.reset();
    }

    @Override
    public long skip(long n) throws IOException {
        ensureDelegate();
        return super.skip(n);
    }

    protected void ensureDelegate() throws IOException {
        if (in == null) {
            synchronized (this) {
                if (in == null) {
                    try {
                        in = creator.call();
                    } catch (IOException e) {
                        throw e;
                    } catch (Exception x) {
                        throw new IOException(x);
                    }
                }
            }
        }
    }

    @Override
    protected void beforeRead(int n) throws IOException {
        ensureDelegate();
        super.beforeRead(n);
    }
}
