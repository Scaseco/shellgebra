package org.aksw.vshell.registry;

import java.util.Optional;

public interface CommandBindingLocator {
    Optional<CommandBinding> locate(String virtualCommandName);
}
