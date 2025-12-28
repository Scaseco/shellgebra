package org.aksw.vshell.registry;

import java.util.Optional;

public interface CommandParserCatalog {
    Optional<JvmCommandParser> getParser(String commandName);
}
