package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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

//    @Override
//    public String which(String cmdName) throws IOException, InterruptedException {
//        int n = locatorCommand.length;
//        String[] argv = Arrays.copyOf(locatorCommand, n + 1);
//        argv[n] = cmdName;
//        String result = execCmd(argv);
//        return result;
//    }

    @Override
    public String execCmd(String... argv) throws IOException, InterruptedException {
        Container.ExecResult execResult = container.execInContainer(StandardCharsets.UTF_8, argv);
        String result = execResult.getStdout();
        return result;
    }

    @Override
    public int runCmd(String... argv) throws IOException, InterruptedException {
        Container.ExecResult execResult = container.execInContainer(StandardCharsets.UTF_8, argv);
        int result = execResult.getExitCode();
        return result;
    }

//    @Override
//    public String quoteFileArgument(String fileName) {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public CmdString compileString(CmdOp op) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public String[] compileCommand(CmdOp op) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public CmdStrOps getStrOps() {
//        return cmdStrOps;
//    }
//
//    /** Not supported. This method may need to be made container aware - i.e. distinguish between host and container path. */
//    // XXX Could create a named pipe in the container - but we don't have any bind mount
//    // We could bind mount a directory though and created pipes on demand
//    @Override
//    public void createNamedPipe(Path path) throws IOException {
//        throw new UnsupportedOperationException();
//    }

    @Override
    public void close() {
        container.stop();
    }
}
