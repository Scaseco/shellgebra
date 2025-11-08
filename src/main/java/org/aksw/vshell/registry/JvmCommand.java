package org.aksw.vshell.registry;

import org.aksw.shellgebra.exec.Stage;
import org.aksw.vshell.shim.rdfconvert.Args;

public interface JvmCommand {
    Args parseArgs(String ...args);
    Stage newStage(String... args);
}
