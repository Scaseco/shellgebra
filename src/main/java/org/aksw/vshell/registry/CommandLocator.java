package org.aksw.vshell.registry;

import java.util.Optional;

public interface CommandLocator {
    Optional<String> locate(String command);
}
