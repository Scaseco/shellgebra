package org.aksw.shellgebra.unused.algebra.plan;

import org.apache.commons.cli.CommandLine;

import jenax.engine.qlever.docker.ContainerDef;

/**
 * Execution of a command using a docker container.
 */
public class CommandEnvDocker
    extends CommandEnv
{
    protected ContainerDef containerDef;

    public CommandEnvDocker(CommandLine commandLine, ContainerDef conatinerDef) {
        super(commandLine);
        this.containerDef = conatinerDef;
    }

    public ContainerDef getContainerDef() {
        return containerDef;
    }
}
