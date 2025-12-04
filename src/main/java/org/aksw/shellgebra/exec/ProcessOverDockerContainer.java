package org.aksw.shellgebra.exec;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import org.aksw.commons.util.docker.ContainerUtils;
import org.testcontainers.containers.GenericContainer;

public class ProcessOverDockerContainer
    extends Process
{
    private GenericContainer<?> container;

    public ProcessOverDockerContainer(GenericContainer<?> container) {
        super();
        this.container = Objects.requireNonNull(container);
    }

    @Override
    public OutputStream getOutputStream() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream getInputStream() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream getErrorStream() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int waitFor() throws InterruptedException {
        ContainerUtils.waitFor(container);
        return ContainerUtils.getExitValue(container);
    }

    @Override
    public int exitValue() {
        // XXX Cache exit value?
        // XXX An exit callback might be best - bu gemini claimed that the exit value cannot be extracted from a one-shot wait callback.
        return ContainerUtils.getExitValue(container);
    }

    @Override
    public void destroy() {
        container.stop();
        // XXX We could probably also remove the container here - but we need to make sure to extract the exit value first.
    }
}
