package org.aksw.vshell.registry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.aksw.shellgebra.exec.graph.PosixPipe;
import org.apache.commons.io.output.ProxyOutputStream;


/**
 * Dynamically add a pipe that other processes can write to.
 *
 * Note right now an upgrade to a pipe sends all writes to the pipe.
 * Actually we can still write directly to the sink.
 * The point is, that the pipe acts as an <b>additional</b> endpoint for writing
 * to the actual output stream.
 */
public class DynamicOutputFromStream
    extends OutputBase
    implements DynamicOutput
{
    @Override
    public OutputStream openOutputStream() throws IOException {
        return new DynamicOutputStream(coreOutput);
    }

    protected OutputStream coreOutput;
    protected PosixPipe pipe = null;
    protected CompletableFuture<?> pumpFuture = null;
    protected Object lock = new Object();

    protected DynamicOutputFromStream(OutputStream coreOutput) {
        super(null);
        this.coreOutput = coreOutput;
    }

    public static DynamicOutputFromStream of(OutputStream coreOutput) {
        return new DynamicOutputFromStream(coreOutput);
    }

    @Override
    public boolean hasFile() {
        return pipe != null;
    }

    @Override
    public Path getFile() throws IOException {
        upgrade();
        return pipe.getWriteEndProcPath();
    }

    protected void upgrade() throws IOException {
        if (pipe == null) {
            synchronized (lock) {
                if (pipe == null) {
                    pipe = PosixPipe.open();
                    ((DynamicOutputStream)outputStream()).setDelegate(pipe.getOutputStream());
                    Runnable runnable = () -> {
                        try (InputStream in = pipe.getInputStream()) {
                        // InputStream in = pipe.getInputStream();
                        // try {
                            in.transferTo(coreOutput);
                            coreOutput.flush();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } finally {
                            // System.out.println("Pump thread pipe -> output terminated.");
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
            if (pipe != null) {
                pipe.getOutputStream().close();
            }
        } finally {
            try {
                if (pumpFuture != null) {
                    // pumpFuture.cancel(true);
                    pumpFuture.join();
                }
            } finally {
                try {
                    if (pipe != null) {
                        pipe.close(); // same as pipe.getInputStream().close();
                    }
                } finally {
                    coreOutput.close();
                }
            }
        }
    }

//    public void join() {
//        if (pumpFuture != null) {
//            pumpFuture.join();
//        }
//    }

    private static class DynamicOutputStream
        extends ProxyOutputStream {

        public DynamicOutputStream(OutputStream proxy) {
            super(proxy);
        }

        public void setDelegate(OutputStream proxy) {
            this.out = proxy;
        }

        // Could lock beforeRead / afterRead to prevent upgrade in between
    }
}
