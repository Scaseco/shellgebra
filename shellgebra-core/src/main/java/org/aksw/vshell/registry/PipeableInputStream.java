package org.aksw.vshell.registry;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.aksw.shellgebra.exec.graph.PosixPipe;
import org.apache.commons.io.input.ProxyInputStream;

/**
 * Holder for an InputStream that can on-demand "upgrade" the input stream
 * to pass through a pipe.
 */
@Deprecated // Should not be needed because we always start with a file resource.
public class PipeableInputStream {
    private InputStream rawInputStream;
    private PosixPipe pipe = null;
    private Thread pumpThread = null;

    private InputStream finalInputStream;
    private final Object lock = new Object();

    protected PipeableInputStream(InputStream rawInputStream) {
        super();
        this.rawInputStream = rawInputStream;
    }

    public PipeableInputStream of(InputStream inputStream) {
        Objects.requireNonNull(inputStream);
        return new PipeableInputStream(inputStream);
    }

    /** Whether the pipe has already been constructed. */
    public boolean hasPipeBeenCreated() {
        return pipe != null;
    }

    public PosixPipe asPosixPipe() throws IOException {
        if (pipe != null) {
            synchronized (lock) {
                if (pipe != null) {
                    if (finalInputStream == null) {
                        throw new RuntimeException("InputStream has already been accessed.");
                    }

                    pipe = PosixPipe.open();
                    Runnable runnable = () -> {
                        try {
                            rawInputStream.transferTo(pipe.getOutputStream());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    };
                    pumpThread = new Thread(runnable);
                    pumpThread.start();
                    finalInputStream = new ProxyInputStream(pipe.getInputStream()) {
                        @Override
                        public void close() throws IOException {
                            try {
                                pipe.close();
                                try {
                                    pumpThread.join();
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            } finally {
                                super.close();
                            }
                        }
                    };
                }
            }
        }
        return pipe;
    }

    public InputStream getInputStream() {
        if (finalInputStream == null) {
            synchronized (lock) {
                if (finalInputStream == null) {
                    finalInputStream = rawInputStream;
                }
            }
        }
        return finalInputStream;
    }
}
