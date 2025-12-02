package org.aksw.vshell.shim.rdfconvert;

import org.aksw.commons.util.docker.Argv;
import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.aksw.vshell.registry.JvmCommand;

/**
 * Base class that parses arguments and passes them to the runActual method.
 * Also performs the following default exception handling:
 * An exception during argument parsing results by default in exit code 2.
 * An exception during runActual results by default in exit code 1.
 */
public abstract class JvmCommandBase<T extends Args>
    implements JvmCommand
{
    @Override
    public int run(ProcessRunner cxt, Argv argv) {
        int exitValue = 0;
        T argsModel;
        try {
            argsModel = parseArgs(argv.newArgs());
        } catch (Exception e) {
            e.printStackTrace(cxt.internalPrintErr());
            exitValue = 2;
            return exitValue;
        }

        try {
            runActual(cxt, argsModel);
            return 0;
        } catch (Exception e) {
            e.printStackTrace(cxt.internalPrintErr());
            return 1;
        }
    }

    @Override
    public abstract T parseArgs(String... args);

    protected abstract void runActual(ProcessRunner cxt, T argv) throws Exception;
}
