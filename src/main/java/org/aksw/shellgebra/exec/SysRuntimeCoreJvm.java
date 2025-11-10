package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

import org.aksw.commons.util.docker.Argv;
import org.aksw.vshell.registry.JvmCommand;
import org.aksw.vshell.registry.JvmCommandRegistry;

public class SysRuntimeCoreJvm
    implements SysRuntimeCore
{
    private JvmCommandRegistry jvmCmdRegistry;

    public SysRuntimeCoreJvm(JvmCommandRegistry jvmCmdRegistry) {
        super();
        this.jvmCmdRegistry = jvmCmdRegistry;
    }

    @Override
    public IProcessBuilder<?> newProcessBuilder() {
        // return new ProcessBuilderJv
        throw new UnsupportedOperationException();
    }

    @Override
    public String execCmd(String... argv) throws IOException, InterruptedException {
        Argv a = Argv.of(argv);
        String c = a.command();
        String[] s = a.newArgs();
        JvmCommand cmd = jvmCmdRegistry.require(c);
        Stage stage = cmd.newStage(s);
        String result = stage.fromNull().toByteSource().asCharSource(StandardCharsets.UTF_8).read();
        return result;
    }

    @Override
    public int runCmd(String... argv) throws IOException, InterruptedException {
        int result;
        try {
            String tmp = execCmd(argv);
            result = 0;
        } catch (NoSuchElementException e) {
            result = 127;
        }
        return result;
    }

    @Override
    public void close() {
    }
}
