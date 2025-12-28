package org.aksw.vshell.registry;

import java.util.Optional;
import java.util.Set;

import org.aksw.shellgebra.exec.model.ExecSites;

public class CommandParserCatalogImpl
    implements CommandParserCatalog
{
    private CommandCatalog commandCatalog;
    private JvmCommandRegistry commandRegistry;

    public CommandParserCatalogImpl(CommandCatalog commandCatalog, JvmCommandRegistry commandRegistry) {
        super();
        this.commandCatalog = commandCatalog;
        this.commandRegistry = commandRegistry;
    }

    @Override
    public Optional<JvmCommandParser> getParser(String commandName) {
        JvmCommandParser parser = null;
        Set<String> cands = commandCatalog.get(commandName, ExecSites.jvm()).orElse(null);
        if (cands != null) {
            parser = cands.stream().flatMap(c -> commandRegistry.get(c).stream()).findFirst().orElse(null);
        }
        return Optional.ofNullable(parser);
    }
}
