package org.aksw.vshell.registry;

import java.util.Optional;

/** Given a virtual command name, return a physical command name together with an argument mapper. */
public interface CommandBindingLocator {
    Optional<CommandBinding> locate(String virtualCommandName);
}
