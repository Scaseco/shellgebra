package org.aksw.vshell.registry;

import java.util.Optional;

import org.aksw.shellgebra.shim.core.JvmCommandParser;

public interface CommandParserCatalog {
    Optional<JvmCommandParser> getParser(String commandName);
}
