package org.aksw.vshell.registry;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import org.aksw.shellgebra.exec.model.ExecSite;

public class CommandCatalogUnion
    implements CommandSiteCatalog
{
    private List<CommandSiteCatalog> registries;

    public CommandCatalogUnion(List<CommandSiteCatalog> registries) {
        super();
        this.registries = List.copyOf(registries);
    }

    @Override
    public Optional<Multimap<ExecSite, CommandBinding>> get(String virtualCommandName) {
        Multimap<ExecSite, CommandBinding> result = null;
        for (CommandSiteCatalog registry : registries) {
            Multimap<ExecSite, CommandBinding> contrib = registry.get(virtualCommandName).orElse(null);
            if (contrib != null) {
                if (result == null) {
                    result = LinkedHashMultimap.create();
                }
                for (Entry<ExecSite, CommandBinding> e : contrib.entries()) {
                    ExecSite k = e.getKey();
                    CommandBinding v = e.getValue();
                    if (!result.containsKey(k)) {
                        result.put(k, v);
                    }
                }
            }
        }
        return Optional.ofNullable(result);
    }
}
