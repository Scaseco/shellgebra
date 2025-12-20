package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;

/**
 * Process builder to execute commands in a running container via docker exec.
 * This is a limited form of execution because bind mounts cannot by dynamically
 * altered.
 */
public class ProcessBuilderDockerExec
    extends ProcessBuilderBase<ProcessBuilderDockerExec>
{
    private GenericContainer<?> container;

    public ProcessBuilderDockerExec(GenericContainer<?> container) {
        super();
        this.container = container;
    }

    @Override
    public boolean supportsAnonPipeRead() {
        return false;
    }

    @Override
    public boolean supportsAnonPipeWrite() {
        return false;
    }

    @Override
    public boolean supportsDirectNamedPipe() {
        return true;
    }

    @Override
    protected ProcessBuilderDockerExec cloneActual() {
        return new ProcessBuilderDockerExec(container);
    }

    @Override
    public Process start(ProcessRunner executor) throws IOException {
        // TODO Mount the redirects into the container and adjust the command to make use of the redirects:
        // command <input >stdout 2>stderr
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
