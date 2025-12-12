package org.aksw.vshell.registry;

import java.io.PrintStream;
import java.util.List;

import org.aksw.vshell.shim.rdfconvert.ArgsModular;
import org.aksw.vshell.shim.rdfconvert.JvmCommandBase;
import org.apache.commons.exec.ExecuteException;

public class JvmCommandEcho
    extends JvmCommandBase<ArgsEcho>
{
    @Override
    public ArgsModular<ArgsEcho> parseArgs(String... args) {
        ArgsModular<ArgsEcho> result = ArgsEcho.parse(args);
        return result;
    }

    @Override
    public void runActual(JvmExecCxt cxt, ArgsEcho model) throws ExecuteException {
        int exitValue = 0;
        List<String> args = model.getArgs();
        PrintStream out = cxt.out().printStream();
        try {
            boolean isFirst = true;
            for (String arg : args) {
                if (!isFirst) {
                    out.print(" ");
                    isFirst = false;
                }
                out.print(arg);
            }
            out.println();
        } catch (Exception e) {
            e.printStackTrace(cxt.err().printStream());
            exitValue = 1;
        }

        if (exitValue != 0) {
            // TODO Improve exception - should we reuse ExecuteException or roll our own?
            throw new ExecuteException("One or more arguments failed to open as files.", 1);
        }
    }
}
