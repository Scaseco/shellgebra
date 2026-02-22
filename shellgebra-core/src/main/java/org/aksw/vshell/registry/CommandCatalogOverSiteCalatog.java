package org.aksw.vshell.registry;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Multimap;

import org.aksw.shellgebra.exec.model.ExecSite;

public class CommandCatalogOverSiteCalatog
    implements CommandCatalog
{
    private CommandSiteCatalog cmdSiteCatalog;

    private CommandCatalogOverSiteCalatog(CommandSiteCatalog cmdSiteCatalog) {
        super();
        this.cmdSiteCatalog = cmdSiteCatalog;
    }

    public static CommandCatalog of(CommandSiteCatalog cmdSiteCatalog) {
        return new CommandCatalogOverSiteCalatog(cmdSiteCatalog);
    }

    @Override
    public Optional<Collection<String>> get(String commandName) {
        Multimap<ExecSite, CommandBinding> mm = cmdSiteCatalog.get(commandName).orElse(null);
        List<String> names = null;
        if (mm != null) {
            names = mm.asMap().values().stream()
                .flatMap(Collection::stream)
                .map(CommandBinding::commandName)
                .distinct()
                .toList();
        }
        return Optional.ofNullable(names);
    }
}
