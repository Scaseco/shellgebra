package org.aksw.vshell.registry;

import org.aksw.shellgebra.shim.core.ArgsTransform;

public record CommandBinding(String commandName, ArgsTransform argsTransform) {
}
