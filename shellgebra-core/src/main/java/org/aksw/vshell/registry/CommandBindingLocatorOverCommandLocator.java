package org.aksw.vshell.registry;

import java.util.Objects;
import java.util.Optional;

import org.aksw.shellgebra.shim.core.ArgsTransform;

public class CommandBindingLocatorOverCommandLocator
    implements CommandBindingLocator
{
    private CommandLocator commandLocator;

    public CommandBindingLocatorOverCommandLocator(CommandLocator commandLocator) {
        super();
        this.commandLocator = Objects.requireNonNull(commandLocator);
    }

    @Override
    public Optional<CommandBinding> locate(String virtualCommandName) {
        Optional<String> location = commandLocator.locate(virtualCommandName);
        Optional<CommandBinding> result = location.map(loc -> new CommandBinding(loc, ArgsTransform.identity()));
        return result;
    }

    public static CommandBindingLocator of(CommandLocator commandLocator) {
        return new CommandBindingLocatorOverCommandLocator(commandLocator);
    }
}
