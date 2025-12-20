package org.aksw.shellgebra.exec;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ProxyInputStream;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.Container.ExecResult;

public class ProcessDockerExecResult
    extends Process
{
    private Container.ExecResult execResult;
    private OutputStream stdin;
    private InputStreamWrapper stdout;
    private InputStreamWrapper stderr;

    private Object lock = new Object();

    public ProcessDockerExecResult(ExecResult execResult) {
        super();
        this.execResult = execResult;
        this.stdin = OutputStream.nullOutputStream();
        this.stdout = new InputStreamWrapper(new ByteArrayInputStream(execResult.getStdout().getBytes(StandardCharsets.UTF_8)));
        this.stderr = new InputStreamWrapper(new ByteArrayInputStream(execResult.getStderr().getBytes(StandardCharsets.UTF_8)));
    }

    public Container.ExecResult getExecResult() {
        return execResult;
    }

    // Simulates running process as long as stdout / stderr have not been consumed.
    private class InputStreamWrapper
        extends ProxyInputStream {

        private boolean isConsumed = false;

        public InputStreamWrapper(InputStream proxy) {
            super(proxy);
        }

        public boolean isConsumed() {
            return isConsumed;
        }

        @Override
        protected void afterRead(int n) throws IOException {
            super.afterRead(n);
            if (IOUtils.EOF == n) {
                if (!isConsumed) {
                    synchronized (lock) {
                        if (!isConsumed) {
                            isConsumed = true;
                            if (isAllConsumed()) {
                                closeAll();
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isAllConsumed() {
        boolean result = stdout.isConsumed() && stderr.isConsumed();
        return result;
    }

    private void closeAll() {
        IOUtils.closeQuietly(stdin);
        IOUtils.closeQuietly(stdout);
        IOUtils.closeQuietly(stderr);
    }

    @Override
    public OutputStream getOutputStream() {
        return stdin;
    }

    @Override
    public InputStream getInputStream() {
        return stdout;
    }

    @Override
    public InputStream getErrorStream() {
        return stderr;
    }

    @Override
    public int waitFor() throws InterruptedException {
        return 0;
    }

    @Override
    public int exitValue() {
        int exitValue = execResult.getExitCode();
        return exitValue;
    }

    @Override
    public void destroy() {
        closeAll();
    }
}
