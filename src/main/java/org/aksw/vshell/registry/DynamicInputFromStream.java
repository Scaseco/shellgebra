package org.aksw.vshell.registry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.aksw.shellgebra.exec.graph.PosixPipe;
import org.apache.commons.io.input.ProxyInputStream;


/**
 * Input that can "upgrade" to transfer data to an intermediate pipe and read from there instead.
 */
public class DynamicInputFromStream
    extends InputBase
    implements DynamicInput
{
    @Override
    public InputStream openInputStream() throws IOException {
        return new DynamicInputStream(coreInput);
    }

    protected InputStream coreInput;
    protected PosixPipe pipe = null;
    protected CompletableFuture<?> pumpFuture = null;
    protected Object lock = new Object();

    protected DynamicInputFromStream(InputStream coreInput) {
        super(null);
        this.coreInput = coreInput;
    }

    public static DynamicInputFromStream of(InputStream coreInput) {
        return new DynamicInputFromStream(coreInput);
    }

    @Override
    public boolean hasFile() {
        return pipe != null;
    }

    @Override
    public Path getFile() throws IOException {
        upgrade();
        return pipe.getReadEndProcPath();
    }

    protected void upgrade() throws IOException {
        if (pipe == null) {
            synchronized (lock) {
                if (pipe == null) {
                    pipe = PosixPipe.open();
                    ((DynamicInputStream)inputStream()).setDelegate(pipe.getInputStream());
                    Runnable runnable = () -> {
                        try (OutputStream out = pipe.getOutputStream()) {
                            coreInput.transferTo(out);
                            out.flush();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    };
                    pumpFuture = CompletableFuture.runAsync(runnable);
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        try {
            coreInput.close();
        } finally {
            try {
                if (pipe != null) {
                    pipe.close();
                }
            } finally {
                if (pumpFuture != null) {
                    pumpFuture.cancel(true);
                    pumpFuture.join();
                }
            }
        }
    }

    private static class DynamicInputStream
        extends ProxyInputStream {

        public DynamicInputStream(InputStream proxy) {
            super(proxy);
        }

        public void setDelegate(InputStream proxy) {
            this.in = proxy;
        }

        // Could lock beforeRead / afterRead to prevent upgrade in between
    }
}
