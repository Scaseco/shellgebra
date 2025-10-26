package org.aksw.vshell.registry;

import java.util.Map;
import java.util.Optional;

import org.aksw.shellgebra.exec.model.ExecSite;

/**
 * Combine a locator with an exec-site instance.
 * The results returned by the locator will associated with the preset execSiet.
 */
public class CommandRegistryOverLocator
    implements CommandRegistry
{
    private ExecSite execSite;
    private CommandLocator locator;

    public CommandRegistryOverLocator(ExecSite execSite, CommandLocator locator) {
        super();
        this.execSite = execSite;
        this.locator = locator;
    }

    @Override
    public Map<ExecSite, String> get(String virtualCommandName) {
        Optional<String> match = locator.locate(virtualCommandName);
        return match.map(str -> Map.of(execSite, str)).orElse(Map.of());
    }
}
