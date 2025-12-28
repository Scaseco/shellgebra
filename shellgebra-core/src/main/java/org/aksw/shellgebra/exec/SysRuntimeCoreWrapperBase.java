package org.aksw.shellgebra.exec;

import java.io.IOException;

public class SysRuntimeCoreWrapperBase
    implements SysRuntimeCore
{
    private SysRuntimeCore delegate;

    public SysRuntimeCoreWrapperBase(SysRuntimeCore delegate) {
        super();
        this.delegate = delegate;
    }

    public SysRuntimeCore getDelegate() {
        return delegate;
    }

    @Override
    public IProcessBuilder<?> newProcessBuilder() {
        return getDelegate().newProcessBuilder();
    }

    @Override
    public String execCmd(String... argv) throws IOException, InterruptedException {
        return getDelegate().execCmd(argv);
    }

    @Override
    public int runCmd(String... argv) throws IOException, InterruptedException {
        return getDelegate().runCmd(argv);
    }

    @Override
    public void close() {
        getDelegate().close();
    }
}
