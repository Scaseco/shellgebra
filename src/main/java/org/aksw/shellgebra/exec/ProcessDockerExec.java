package org.aksw.shellgebra.exec;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.testcontainers.DockerClientFactory;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StreamType;

/**
 * A minimal Process-like wrapper over `docker exec` using the modern
 * ResultCallback API. - Separate streaming stdout/stderr (TTY=false) -
 * Streaming stdin - waitFor/exitValue/isAlive - Best-effort destroy (close
 * stdin + stop callback)
 */
public class ProcessDockerExec extends Process {
    private final DockerClient docker;
    private final String containerId;
    private final String execId;

    // Client-facing streams
    private final PipedInputStream stdoutIn = new PipedInputStream();
    private final PipedInputStream stderrIn = new PipedInputStream();
    private final PipedOutputStream stdinOut = new PipedOutputStream();

    // Internal ends that we write/read to
    private final PipedOutputStream stdoutSink;
    private final PipedOutputStream stderrSink;
    private final PipedInputStream stdinIn;

    private final CountDownLatch finished = new CountDownLatch(1);
    private volatile Long exitCode = null;
    private volatile boolean started = false;
    private volatile ResultCallback<Frame> callback;

    public ProcessDockerExec(String containerId, String... cmd) throws IOException {
        this.docker = DockerClientFactory.instance().client();
        this.containerId = Objects.requireNonNull(containerId, "containerId");

        // Wire pipes
        this.stdoutSink = new PipedOutputStream(stdoutIn);
        this.stderrSink = new PipedOutputStream(stderrIn);
        this.stdinIn = new PipedInputStream(stdinOut);

        // Create exec
        ExecCreateCmdResponse created = docker.execCreateCmd(containerId).withAttachStdout(true).withAttachStderr(true)
                .withAttachStdin(true).withTty(false) // keep stdout & stderr separated
                .withCmd(cmd).exec();

        this.execId = created.getId();

        // Start streaming
        this.callback = docker.execStartCmd(execId).withDetach(false).withTty(false).withStdIn(stdinIn)
                .exec(new ResultCallback.Adapter<Frame>() {
                    @Override
                    public void onStart(Closeable closeable) {
                        started = true;
                    }

                    @Override
                    public void onNext(Frame frame) {
                        try {
                            if (frame.getStreamType() == StreamType.STDOUT) {
                                stdoutSink.write(frame.getPayload());
                                stdoutSink.flush();
                            } else if (frame.getStreamType() == StreamType.STDERR) {
                                stderrSink.write(frame.getPayload());
                                stderrSink.flush();
                            }
                        } catch (IOException e) {
                            // Propagate error to completion path
                            onError(e);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        // Attempt to capture an exit code (may still be null if truly broken)
                        try {
                            exitCode = docker.inspectExecCmd(execId).exec().getExitCodeLong();
                        } catch (Exception ignore) {
                        }
                        finished.countDown();
                        // Let Adapter handle logging/default behavior
                        super.onError(throwable);
                    }

                    @Override
                    public void onComplete() {
                        try {
                            stdoutSink.flush();
                            stderrSink.flush();
                        } catch (IOException ignore) {
                        }
                        try {
                            exitCode = docker.inspectExecCmd(execId).exec().getExitCodeLong();
                        } catch (Exception ignore) {
                        }
                        finished.countDown();
                        super.onComplete();
                    }
                });
    }

    public String getContainerId() {
        return containerId;
    }

    // ----- Process API -----

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

    @Override
    public int waitFor() throws InterruptedException {
        finished.await();
        return exitValue();
    }

    @Override
    public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
        boolean done = finished.await(timeout, unit);
        if (done)
            exitValue(); // may throw if still null
        return done;
    }

    @Override
    public int exitValue() {
        Long code = exitCode;
        if (code == null) {
            try {
                // XXX This seems redundant - container termination event should set the exit code.
                code = docker.inspectExecCmd(execId).exec().getExitCodeLong();
            } catch (Exception ignore) {
            }
        }
        if (code == null)
            throw new IllegalThreadStateException("Process not yet finished");
        return code.intValue();
    }

    @Override
    public void destroy() {
        // Best-effort: close stdin (EOF) and close the callback (stops streaming)
        try {
            stdinOut.close();
        } catch (IOException ignore) {
        }
        try {
            if (callback != null)
                callback.close();
        } catch (IOException ignore) {
        }
    }

    @Override
    public Process destroyForcibly() {
        // There is no first-class "kill exec" API; closing streams & callback is the
        // portable option.
        destroy();
        return this;
    }

    @Override
    public boolean isAlive() {
        if (!started)
            return true; // streaming started but no exit yet
        try {
            // XXX Waiting for exit code from termination event should be sufficient.
            return docker.inspectExecCmd(execId).exec().getExitCodeLong() == null;
        } catch (Exception e) {
            return false;
        }
    }

    // Optional helper to read all output (blocking) — handy for tests
    public String readAllStdout() throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try (InputStream in = getInputStream()) {
            in.transferTo(buf);
        }
        return buf.toString(StandardCharsets.UTF_8);
    }
}
