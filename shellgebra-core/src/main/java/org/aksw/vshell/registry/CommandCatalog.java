package org.aksw.vshell.registry;

import java.util.Collection;
import java.util.Optional;

public interface CommandCatalog {
    /**
     * Return an optional list of candidate locations for the command.
     * Empty optional means no information present for the given command.
     * Empty collection means that it is known that there are no locations.
     *
     * @param commandName
     * @return
     */
    Optional<Collection<String>> get(String commandName);

    // Could add this method and by default have it raise UnsupportedOperationException.
    // CommandCatalog put(String commandName, String candidateLocation);
}
