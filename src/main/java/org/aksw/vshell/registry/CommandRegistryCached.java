package org.aksw.vshell.registry;

import java.util.Objects;

import org.aksw.shellgebra.exec.model.ExecSite;

import com.google.common.collect.Multimap;

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
    public Multimap<ExecSite, String> get(String virtualCommandName) {
        Multimap<ExecSite, String> result = dynamicRegistry.getKnownExecSites(virtualCommandName).orElse(null);
        if (result == null) {
            result = baseRegistry.get(virtualCommandName);
            dynamicRegistry.putAll(virtualCommandName, result);
        }
        return result;
    }
}
