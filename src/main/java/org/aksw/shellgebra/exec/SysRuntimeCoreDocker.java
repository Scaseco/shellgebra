package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.aksw.vshell.registry.JvmExecUtils;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;

/** Wrapper for a running container. */
public class SysRuntimeCoreDocker
    implements SysRuntimeCore
    // implements SysRuntime
{
    private GenericContainer<?> container;
    private CmdStrOps cmdStrOps;

    public SysRuntimeCoreDocker(GenericContainer<?> container, CmdStrOps cmdStrOps) {
        super();
        this.container = container;
        this.cmdStrOps = cmdStrOps;
    }

    public String getImageRef() {
        return container.getDockerImageName();
    }

    public CmdStrOps getCmdStrOps() {
        return cmdStrOps;
    }

    @Override
    public IProcessBuilder<?> newProcessBuilder() {
        return new ProcessBuilderDocker(container);
    }

    @Override
    public String execCmd(String... argv) throws IOException, InterruptedException {
        Container.ExecResult execResult = container.execInContainer(StandardCharsets.UTF_8, argv);
        String result = execResult.out();
        // Remove trailing newline
        result = JvmExecUtils.removeTrailingNewline(result);
        return result;
    }

    @Override
    public int runCmd(String... argv) throws IOException, InterruptedException {
        Container.ExecResult execResult = container.execInContainer(StandardCharsets.UTF_8, argv);
        int result = execResult.getExitCode();
        return result;
    }

    @Override
    public void close() {
        container.stop();
    }
}
