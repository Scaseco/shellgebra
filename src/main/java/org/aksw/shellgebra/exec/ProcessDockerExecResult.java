package org.aksw.shellgebra.exec;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.testcontainers.containers.Container;
import org.testcontainers.containers.Container.ExecResult;

public class ProcessDockerExecResult
    extends Process
{
    private Container.ExecResult execResult;

    public ProcessDockerExecResult(ExecResult execResult) {
        super();
        this.execResult = execResult;
    }

    @Override
    public OutputStream getOutputStream() {
        return OutputStream.nullOutputStream();
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(execResult.getStdout().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public InputStream getErrorStream() {
        return new ByteArrayInputStream(execResult.getStderr().getBytes(StandardCharsets.UTF_8));
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
    }
}
