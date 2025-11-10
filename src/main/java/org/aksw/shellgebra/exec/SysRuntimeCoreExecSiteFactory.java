package org.aksw.shellgebra.exec;

import org.aksw.shellgebra.exec.model.ExecSite;

public interface SysRuntimeCoreExecSiteFactory
    extends AutoCloseable
{
    SysRuntimeCore getRuntime(ExecSite execSite);

    // Close all runtimes managed by this instance.
    @Override
    void close();
}
