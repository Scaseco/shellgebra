package org.aksw.vshell.registry;

import java.util.Optional;
import java.util.Set;

import org.aksw.shellgebra.exec.model.ExecSites;
import org.aksw.shellgebra.shim.core.JvmCommandParser;

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

    /**
     * Searches for a parser by looking of the command name on the 'jvm' execution site.
     * Picks the first command on the jvm exec site.
     */
    @Override
    public Optional<JvmCommandParser> getParser(String commandName) {
        JvmCommandParser parser = null;
        Set<CommandBinding> cands = commandCatalog.get(commandName, ExecSites.jvm()).orElse(null);
        if (cands != null) {
            parser = cands.stream().flatMap(c -> commandRegistry.get(c.commandName()).stream()).findFirst().orElse(null);
        }
        return Optional.ofNullable(parser);
    }
}
