package org.aksw.vshell.registry;

import java.util.Optional;

// TODO Perhaps command locator should be removed because its just one feature of SysRuntimeCore.
public interface CommandLocator {
    Optional<String> locate(String command);
}
