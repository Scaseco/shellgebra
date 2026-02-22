package org.aksw.shellgebra.processbuilder;

import java.util.Objects;

import org.aksw.commons.util.docker.ContainerUtils;
import org.aksw.vshell.registry.ProcessBase;
import org.testcontainers.containers.GenericContainer;

public class ProcessOverDockerContainer
    extends ProcessBase
{
    private GenericContainer<?> container;

    protected ProcessOverDockerContainer(GenericContainer<?> container, OutboundIo outboundIo) {
        super(outboundIo);
        this.container = Objects.requireNonNull(container);
    }

    public static Process of(GenericContainer<?> container, OutboundIo outboundIo) {
        return new ProcessOverDockerContainer(container, outboundIo);
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
