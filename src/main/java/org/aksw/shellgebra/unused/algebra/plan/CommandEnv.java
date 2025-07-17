package org.aksw.shellgebra.unused.algebra.plan;

public class CommandEnv {
    // protected CommandLine commandLine;
    protected String toolName;
    protected String resolvedCommand;

    public CommandEnv(String toolName, String resolvedCommand) {
        super();
        this.toolName = toolName;
        this.resolvedCommand = resolvedCommand;
    }


//    public CommandEnv(CommandLine commandLine) {
//        super();
//        this.commandLine = commandLine;
//    }

//    public CommandLine getCommandLine() {
//        return commandLine;
//    }
}
