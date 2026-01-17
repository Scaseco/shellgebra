package org.aksw.shellgebra.introspect;

import java.util.Collection;

public record LocatorCommand2(
    String name, /** Shell name, such as bash, zsh, dash, etc. */
    Collection<String> probeLocations
) {}
