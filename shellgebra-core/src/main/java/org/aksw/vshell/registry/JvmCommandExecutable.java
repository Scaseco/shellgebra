package org.aksw.vshell.registry;

import org.aksw.commons.util.docker.Argv;

public interface JvmCommandExecutable {
    // XXX Alternatively: void run(JvmExecCxt cxt, Argv argv) throws ExecuteException
    int run(JvmExecCxt cxt, Argv argv);
}
