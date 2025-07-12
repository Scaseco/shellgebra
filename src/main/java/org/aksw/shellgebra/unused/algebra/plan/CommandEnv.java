package org.aksw.shellgebra.unused.algebra.plan;

import org.apache.commons.cli.CommandLine;

public class CommandEnv {
    protected CommandLine commandLine;

    public CommandEnv(CommandLine commandLine) {
        super();
        this.commandLine = commandLine;
    }

    public CommandLine getCommandLine() {
        return commandLine;
    }
}
