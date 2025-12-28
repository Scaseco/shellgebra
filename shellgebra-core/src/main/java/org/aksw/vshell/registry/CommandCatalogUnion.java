package org.aksw.vshell.registry;

import java.util.List;

import org.aksw.shellgebra.exec.model.ExecSite;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public class CommandCatalogUnion
    implements CommandCatalog
{
    private List<CommandCatalog> registries;

    public CommandCatalogUnion(List<CommandCatalog> registries) {
        super();
        this.registries = List.copyOf(registries);
    }

    @Override
    public Multimap<ExecSite, String> get(String virtualCommandName) {
        Multimap<ExecSite, String> result = LinkedHashMultimap.create();
        for (CommandCatalog registry : registries) {
            Multimap<ExecSite, String> contrib = registry.get(virtualCommandName);
            contrib.forEach((k, v) -> {
                if (!result.containsKey(k)) {
                    result.put(k, v);
                }
            });
        }
        return result;
    }
}
