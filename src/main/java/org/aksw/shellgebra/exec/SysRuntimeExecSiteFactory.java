package org.aksw.shellgebra.exec;

import org.aksw.shellgebra.exec.model.ExecSite;

public interface SysRuntimeExecSiteFactory
{
    SysRuntimeFactory getFactory(ExecSite execSite);
}
