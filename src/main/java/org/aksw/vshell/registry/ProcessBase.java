package org.aksw.vshell.registry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public abstract class ProcessBase
    extends Process
{
    // Client-facing streams
    private final PipedInputStream stdoutIn = new PipedInputStream();
    private final PipedInputStream stderrIn = new PipedInputStream();
    private final PipedOutputStream stdinOut = new PipedOutputStream();

    // Internal ends that we write/read to
    private final PipedOutputStream stdoutSink;
    private final PipedOutputStream stderrSink;
    private final PipedInputStream stdinIn;

    private volatile Integer exitValue = null;

    public ProcessBase() {
        super();
        // Wire pipes
        try {
            this.stdoutSink = new PipedOutputStream(stdoutIn);
            this.stderrSink = new PipedOutputStream(stderrIn);
            this.stdinIn = new PipedInputStream(stdinOut);
        } catch (IOException e) {
            throw new IllegalStateException("Should not happen", e);
        }
    }

    public PipedOutputStream getStdoutSink() {
        return stdoutSink;
    }

    public PipedOutputStream getStderrSink() {
        return stderrSink;
    }

    public PipedInputStream getStdinIn() {
        return stdinIn;
    }

    @Override
    public OutputStream getOutputStream() {
        return stdinOut;
    }

    @Override
    public InputStream getInputStream() {
        return stdoutIn;
    }

    @Override
    public InputStream getErrorStream() {
        return stderrIn;
    }

    protected void setExitValue(Integer exitValue) {
        this.exitValue = exitValue;
    }

    @Override
    public int exitValue() {
        if (exitValue == null) {
            throw new IllegalThreadStateException("Thread has not yet terminated");
        }
        return exitValue.intValue();
    }
}
