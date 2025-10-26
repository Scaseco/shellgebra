package org.aksw.vshell.registry;

import org.aksw.shellgebra.exec.Stage;

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
}
