package org.aksw.vshell.registry;

import java.util.List;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import org.aksw.shellgebra.exec.model.ExecSite;

public class CommandCatalogUnion
    implements CommandCatalog
{
    private List<CommandCatalog> registries;

    public CommandCatalogUnion(List<CommandCatalog> registries) {
        super();
        this.registries = List.copyOf(registries);
    }

    @Override
    public Multimap<ExecSite, CommandBinding> get(String virtualCommandName) {
        Multimap<ExecSite, CommandBinding> result = LinkedHashMultimap.create();
        for (CommandCatalog registry : registries) {
            Multimap<ExecSite, CommandBinding> contrib = registry.get(virtualCommandName);
            contrib.forEach((k, v) -> {
                if (!result.containsKey(k)) {
                    result.put(k, v);
                }
            });
        }
        return result;
    }
}
