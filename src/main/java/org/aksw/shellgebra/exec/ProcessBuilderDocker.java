package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;

public class ProcessBuilderDocker
    extends ProcessBuilderBase<ProcessBuilderDocker>
{
    private GenericContainer<?> container;

    public ProcessBuilderDocker(GenericContainer<?> container) {
        super();
        this.container = container;
    }

    @Override
    public ProcessDockerExecResult start() throws IOException {
        String[] argv = Objects.requireNonNull(command()).toArray(String[]::new);
        Container.ExecResult execResult;
        try {
            execResult = container.execInContainer(StandardCharsets.UTF_8, argv);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return new ProcessDockerExecResult(execResult);
    }
}
