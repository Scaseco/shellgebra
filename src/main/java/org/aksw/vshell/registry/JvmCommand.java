package org.aksw.vshell.registry;

import org.aksw.shellgebra.exec.Stage;

public interface JvmCommand {
    public Stage newStage(String... args);
}
