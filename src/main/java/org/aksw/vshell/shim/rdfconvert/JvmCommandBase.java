package org.aksw.vshell.shim.rdfconvert;

import org.aksw.commons.util.docker.Argv;
import org.aksw.vshell.registry.JvmCommand;
import org.aksw.vshell.registry.JvmExecCxt;

/**
 * Base class that parses arguments and passes them to the runActual method.
 * Also performs the following default exception handling:
 * An exception during argument parsing results by default in exit code 2.
 * An exception during runActual results by default in exit code 1.
 */
public abstract class JvmCommandBase<T>
    implements JvmCommand
{
    @Override
    public int run(JvmExecCxt cxt, Argv argv) {
        int exitValue = 0;
        T argsModel;
        try {
            ArgsModular<T> argsModular = parseArgs(argv.newArgs());
            argsModel = argsModular.model();
        } catch (Exception e) {
            e.printStackTrace(cxt.err().printStream());
            exitValue = 2;
            return exitValue;
        }

        try {
            runActual(cxt, argsModel);
            return 0;
        } catch (Exception e) {
            e.printStackTrace(cxt.err().printStream());
            return 1;
        }
    }

    @Override
    public abstract ArgsModular<T> parseArgs(String... args);

    protected abstract void runActual(JvmExecCxt cxt, T argv) throws Exception;
}
