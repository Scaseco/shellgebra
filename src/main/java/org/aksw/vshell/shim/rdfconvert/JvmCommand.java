package org.aksw.vshell.shim.rdfconvert;

import org.aksw.shellgebra.exec.Stage;

public interface JvmCommand {
    public Stage newStage(String... args);
}
