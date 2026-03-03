package org.aksw.shellgebra.exec;

import java.io.IOException;

import org.aksw.shellgebra.util.SystemUtils;
import org.aksw.vshell.registry.ProcessBuilderNative;

public class SysRuntimeCoreHost
    implements SysRuntimeCore
{
    private static final SysRuntimeCoreHost INSTANCE = new SysRuntimeCoreHost();

    protected SysRuntimeCoreHost() {
    }

    public static SysRuntimeCore get() {
        return INSTANCE;
    }

    @Override
    public IProcessBuilder<?> newProcessBuilder() {
        return new ProcessBuilderNative();
    }

    @Override
    public String execCmd(String... argv) throws IOException, InterruptedException {
        String result = SystemUtils.getCommandOutput(argv);
        return result;
    }

    @Override
    public int runCmd(String... argv) throws IOException, InterruptedException {
        int exitValue = SystemUtils.runCmd(argv);
        return exitValue;
    }

    @Override
    public void close() {
    }
}
