package org.aksw.vshell.registry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.aksw.shellgebra.exec.model.ExecSite;

public class CommandRegistryUnion
    implements CommandRegistry
{
    private List<CommandRegistry> registries;

    public CommandRegistryUnion(List<CommandRegistry> registries) {
        super();
        this.registries = List.copyOf(registries);
    }

    @Override
    public Map<ExecSite, String> get(String virtualCommandName) {
        Map<ExecSite, String> result = new LinkedHashMap<>();
        for (CommandRegistry registry : registries) {
            Map<ExecSite, String> contrib = registry.get(virtualCommandName);
            contrib.forEach((k, v) -> {
                if (!result.containsKey(k)) {
                    result.put(k, v);
                }
            });
        }
        return result;
    }
}
