package org.aksw.shellgebra.introspect;

import java.util.Collection;

public record ShellCatalogEntry(
    String name, /** Shell name, such as bash, zsh, dash, etc. */
    Collection<String> probeLocations,
    String commandOption, // Alternative: scriptString- > args - Function<String, Args>
    String builtInLocator
) {}
