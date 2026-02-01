package org.aksw.vshell.registry;

import java.util.Optional;

public class CommandLocatorNull
    implements CommandLocator
{
    private static final CommandLocator INSTANCE = new CommandLocatorNull();

    private CommandLocatorNull() {
        super();
    }

    @Override
    public Optional<String> locate(String command) {
        return Optional.empty();
    }

    public static CommandLocator get() {
        return INSTANCE;
    }
}
