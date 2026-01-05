package org.aksw.shellgebra.model.osreo;

import java.util.Map;

public interface ImageIntrospection {
    // @Iri // ("shellSupport")
    // Set<ShellSupport> getShellSupport();
    Map<String, ShellSupport> getShellStatus();

    ShellSupport getOrCreateShellSupport(String shellName);

    //ShellSupport getOrAddShellSupport(String shellName) {
    //
    //}

    // "Which" resolutions.
    // Map<String, String> getWhichMap();
}
