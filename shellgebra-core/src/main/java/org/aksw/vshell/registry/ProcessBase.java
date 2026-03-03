package org.aksw.vshell.registry;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;

import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.apache.commons.io.IOUtils;

public abstract class ProcessBase
    extends Process
{
    /**
     * Record that holds the IO streams a process exposes to the outside via
     * {@link Process#getInputStream()}, {@link Process#getOutputStream()} and {@link Process#getErrorStream()}.
     */
    public record OutboundIo(OutputStream toIn, InputStream fromOut, InputStream fromErr) {}

    public static OutboundIo getOutboundIo(Process process) {
        return new OutboundIo(process.getOutputStream(), process.getInputStream(), process.getErrorStream());
    }

    public static OutboundIo getOutboundIo(ProcessRunner context) {
        return new OutboundIo(context.getOutputStream(), context.getInputStream(), context.getErrorStream());
    }

    public interface HasOutboundIo {
        OutboundIo getOutboundIo();
    }

    public record ToInternalIo(
        DynamicInputShared fromIn,
        DynamicOutputShared toOut,
        DynamicOutputShared toErr)
    implements Closeable {
        @Override
        public void close() {
            IOUtils.closeQuietly(fromIn);
            IOUtils.closeQuietly(toOut);
            IOUtils.closeQuietly(toErr);
        }
    }

    // private OutboundIo outboundIo;

    // Client-facing streams
    private final OutputStream toIn;
    private final InputStream fromOut;
    private final InputStream fromErr;
//    private final PipedInputStream stdoutIn = new PipedInputStream();
//    private final PipedInputStream stderrIn = new PipedInputStream();
//    private final PipedOutputStream stdinOut = new PipedOutputStream();

    // Internal ends that we write/read to
//    private final PipedOutputStream stdoutSink;
//    private final PipedOutputStream stderrSink;
//    private final PipedInputStream stdinIn;

    private volatile Integer exitValue = null;

    public ProcessBase(OutboundIo io) {
        this(io.toIn(), io.fromOut(), io.fromErr());
    }

    public ProcessBase(OutputStream toIn, InputStream fromOut, InputStream fromErr) {
        super();
        this.toIn = toIn;
        this.fromOut = fromOut;
        this.fromErr = fromErr;
    }

//    public ProcessBase() {
//        super();
//        // Wire pipes
//        try {
//            this.stdoutSink = new PipedOutputStream(stdoutIn);
//            this.stderrSink = new PipedOutputStream(stderrIn);
//            this.stdinIn = new PipedInputStream(stdinOut);
//        } catch (IOException e) {
//            throw new IllegalStateException("Should not happen", e);
//        }
//    }

//    public PipedOutputStream getStdoutSink() {
//        return stdoutSink;
//    }
//
//    public PipedOutputStream getStderrSink() {
//        return stderrSink;
//    }
//
//    public PipedInputStream getStdinIn() {
//        return stdinIn;
//    }

    @Override
    public OutputStream getOutputStream() {
        return toIn;
    }

    @Override
    public InputStream getInputStream() {
        return fromOut;
    }

    @Override
    public InputStream getErrorStream() {
        return fromErr;
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
