package org.aksw.vshell.registry;

import org.aksw.commons.util.docker.Argv;
import org.aksw.vshell.shim.rdfconvert.Args;

public interface JvmCommand {
    Args parseArgs(String... args);
    // Stage newStage(String... args);

    int run(JvmExecCxt cxt, Argv argv);
}
