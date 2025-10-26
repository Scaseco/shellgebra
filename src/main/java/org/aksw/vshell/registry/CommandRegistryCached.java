package org.aksw.vshell.registry;

import java.util.Map;
import java.util.Objects;

import org.aksw.shellgebra.exec.model.ExecSite;

public class CommandRegistryCached
    implements CommandRegistry
{
    private CommandRegistry baseRegistry;
    private CommandRegistryImpl dynamicRegistry;

    public CommandRegistryCached(CommandRegistry baseRegistry, CommandRegistryImpl dynamicRegistry) {
        super();
        this.baseRegistry = Objects.requireNonNull(baseRegistry);
        this.dynamicRegistry = Objects.requireNonNull(dynamicRegistry);
    }

    @Override
    public Map<ExecSite, String> get(String virtualCommandName) {
        Map<ExecSite, String> result = dynamicRegistry.getKnownExecSites(virtualCommandName).orElse(null);
        if (result == null) {
            result = baseRegistry.get(virtualCommandName);
            dynamicRegistry.putAll(virtualCommandName, result);
        }
        return result;
    }
}
