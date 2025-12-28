package org.aksw.vshell.registry;

import java.util.Optional;

@Deprecated // not needed
public class CommandLocatorWrapper
    implements CommandLocator
{
    private CommandLocator delegate;

    public CommandLocatorWrapper(CommandLocator delegate) {
        super();
        this.delegate = delegate;
    }

    public CommandLocator getDelegate() {
        return delegate;
    }

    @Override
    public Optional<String> locate(String command) {
        return getDelegate().locate(command);
    }
}
