package org.aksw.vshell.registry;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import org.aksw.shellgebra.exec.model.ExecSite;

/**
 * Combine a locator with an exec-site instance.
 * The results returned by the locator will associated with the preset execSiet.
 */
public class CommandCatalogOverLocator
    implements CommandSiteCatalog
{
    private ExecSite execSite;
    private CommandBindingLocator locator;

    public CommandCatalogOverLocator(ExecSite execSite, CommandLocator locator) {
        this(execSite, CommandBindingLocatorOverCommandLocator.of(locator));
    }

    public CommandCatalogOverLocator(ExecSite execSite, CommandBindingLocator locator) {
        super();
        this.execSite = execSite;
        this.locator = Objects.requireNonNull(locator);
    }

    @Override
    public Optional<Multimap<ExecSite, CommandBinding>> get(String virtualCommandName) {
        Optional<CommandBinding> match = locator.locate(virtualCommandName);
        Optional<Multimap<ExecSite, CommandBinding>> result = match.map(str -> Multimaps.forMap(Map.of(execSite, str)));
        return result;
    }
}
