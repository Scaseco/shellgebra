package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.nio.file.Path;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.transform.CmdString;
import org.aksw.vshell.registry.JvmCommandRegistry;

public class SysRuntimeJvm
    implements SysRuntime
{
    private JvmCommandRegistry jvmCmdRegistry;

    @Override
    public String which(String cmdName) throws IOException, InterruptedException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String quoteFileArgument(String fileName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CmdString compileString(CmdOp op) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] compileCommand(CmdOp op) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CmdStrOps getStrOps() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void createNamedPipe(Path path) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

}
