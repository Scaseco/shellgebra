package org.aksw.shellgebra.model.osreo;

import org.aksw.shellgebra.exec.SysRuntimeCoreDocker;
import org.aksw.shellgebra.introspect.ShellCatalogEntry;
import org.aksw.shellgebra.introspect.ShellProbeResult;

/** Introspects images by launching containers. */
public interface ImageIntrospector {
    ShellProbeResult findShell(SysRuntimeCoreDocker runtime, ShellCatalogEntry shell);
    // ShellProbeResult findShell(String image, boolean pullIfAbsent, ShellCatalogEntry shellCatalogEntry);
}
