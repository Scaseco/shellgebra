package org.aksw.vshell.registry;

import java.util.Optional;

public class CommandLocatorJvmRegistry
    implements CommandLocator
{
    private JvmCommandRegistry jvmCmdRegistry;

    public CommandLocatorJvmRegistry(JvmCommandRegistry jvmCmdRegistry) {
        super();
        this.jvmCmdRegistry = jvmCmdRegistry;
    }

    @Override
    public Optional<String> locate(String command) {
        return jvmCmdRegistry.get(command).map(x -> command);
    }

}
