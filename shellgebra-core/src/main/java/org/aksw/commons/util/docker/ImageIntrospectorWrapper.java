package org.aksw.commons.util.docker;

import org.aksw.shellgebra.exec.SysRuntimeCoreDocker;
import org.aksw.shellgebra.model.osreo.ImageIntrospector;
import org.aksw.shellgebra.model2.ShellCatalogEntry;
import org.aksw.shellgebra.model2.ShellProbeResult;

public class ImageIntrospectorWrapper
    implements ImageIntrospector
{
    private ImageIntrospector delegate;

    public ImageIntrospectorWrapper(ImageIntrospector delegate) {
        super();
        this.delegate = delegate;
    }

    public ImageIntrospector getDelegate() {
        return delegate;
    }

    @Override
    public ShellProbeResult findShell(SysRuntimeCoreDocker runtime, ShellCatalogEntry shell) {
        return getDelegate().findShell(runtime, shell);
    }
}
