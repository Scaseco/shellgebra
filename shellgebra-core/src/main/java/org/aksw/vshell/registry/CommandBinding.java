package org.aksw.vshell.registry;

import org.aksw.shellgebra.shim.core.ArgsTransform;

public record CommandBinding(String commandName, ArgsTransform argsTransform) {
    public static CommandBinding of(String commandName, ArgsTransform argsTransform) {
        return new CommandBinding(commandName, argsTransform);
    }

    public static CommandBinding ofIdentity(String commandName) {
        return of(commandName, ArgsTransform.identity());
    }

    public static CommandBinding of(String commandName, String... destinationArgs) {
        return of(commandName, ArgsTransform.noArgs(destinationArgs));
    }
}
