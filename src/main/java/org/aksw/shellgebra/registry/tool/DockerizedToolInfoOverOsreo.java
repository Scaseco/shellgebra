package org.aksw.shellgebra.registry.tool;

import org.aksw.jenax.model.osreo.OsreoTool;

public class DockerizedToolInfoOverOsreo
    implements DockerizedToolInfo
{
    protected OsreoTool delegate;

    @Override
    public String getImageName() {
        return delegate.getImageName();
    }

    @Override
    public String getPreferredImageTag() {
        return delegate.getPreferredImageTag();
    }

    @Override
    public String getCommandName() {
        return delegate.getCommandName();
    }

    @Override
    public String getCommandPath() {
        return delegate.getCommandName();
    }
}
