package org.aksw.vshell.registry;

import org.aksw.shellgebra.exec.Stage;
import org.aksw.vshell.shim.rdfconvert.Args;

public class DockerCommand
    implements JvmCommand
{
    protected String command;
    protected String imageName;

    @Override
    public Stage newStage(String... args) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Args parseArgs(String... args) {
        // TODO Auto-generated method stub
        return null;
    }
}
