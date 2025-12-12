package org.aksw.vshell.shim.rdfconvert;

import java.util.Optional;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;

/**
 * Interface for domain implementations that can be converted to argument lists.
 * The returned argument list uses the {@link CmdArg} model. It allows for tagging files,
 * which is important for placing commands onto docker containers and setting up bind mounts.
 * You can use ArgsModular to separate the model from its rendering as an ArgumentList.
 */
public interface Args {
    ArgumentList toArgList();

    /**
     * Whether this argument configuration would read from stdin.
     * Used to automatically enable/disable interative mode when docker containers are involved.
     */
    Optional<Boolean> readsStdin();
}
