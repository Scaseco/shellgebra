package org.aksw.vshell.registry;

import org.aksw.commons.util.docker.Argv;

public interface JvmCommand
    extends JvmCommandParser
{
    // Args parseArgs(String... args);

    // XXX Alternatively: void run(JvmExecCxt cxt, Argv argv) throws ExecuteException
    int run(JvmExecCxt cxt, Argv argv);
}
