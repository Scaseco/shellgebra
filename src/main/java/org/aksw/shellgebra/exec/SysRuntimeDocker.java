package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.nio.file.Path;

import org.aksw.commons.util.docker.Argv;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.transform.CmdString;

public class SysRuntimeDocker
    implements SysRuntime
{
    // private GenericContainer<?> container;
    // private CmdStrOps cmdStrOps;
    private SysRuntimeCoreDocker core;
    private Argv locatorCommand;
    private Argv existsCommand;

    // private String[] shellCommand;

    public SysRuntimeDocker(SysRuntimeCoreDocker core, Argv locatorCommand, Argv existsCommand) {
        super();
//        this.container = container;
//        this.locatorCommand = locatorCommand;
//        this.cmdStrOps = cmdStrOps;
        this.core = core;
        this.locatorCommand = locatorCommand;
        this.existsCommand = existsCommand;
    }

    @Override
    public String which(String cmdName) throws IOException, InterruptedException {
        String[] argv = ListBuilder.forString().addAll(locatorCommand.argv()).add(cmdName).buildArray();
        String result = execCmd(argv);
        // Remove any trailing newlines.
        return result;
    }

    @Override
    public boolean exists(String cmdName) throws IOException, InterruptedException {
        String[] argv = ListBuilder.forString().addAll(existsCommand.argv()).add(cmdName).buildArray();
        int exitValue = core.runCmd(argv);
        return exitValue == 0;
        // String result = execCmd(argv);
        // Remove any trailing newlines.
        // result = result.replaceAll("\n+$", "");
        // return result;
    }


    protected String execCmd(String... argv) throws UnsupportedOperationException, IOException, InterruptedException {
        String result = core.execCmd(argv);//execInContainer(StandardCharsets.UTF_8, argv);
        return result;
    }

    @Override
    public String quoteFileArgument(String fileName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CmdString compileString(CmdOp op) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] compileCommand(CmdOp op) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CmdStrOps getStrOps() {
        return core.getCmdStrOps();
    }

    /** Not supported. This method may need to be made container aware - i.e. distinguish between host and container path. */
    // XXX Could create a named pipe in the container - but we don't have any bind mount
    // We could bind mount a directory though and created pipes on demand
    @Override
    public void createNamedPipe(Path path) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        core.close();
    }
}
