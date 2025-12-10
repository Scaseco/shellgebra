package org.aksw.vshell.registry;

import java.io.PrintStream;
import java.util.List;

import org.aksw.shellgebra.exec.Stage;
import org.aksw.vshell.shim.rdfconvert.JvmCommandBase;
import org.apache.commons.exec.ExecuteException;

public class JvmCommandEcho
    extends JvmCommandBase<ArgsEcho>
{
    @Override
    public ArgsEcho parseArgs(String... args) {
        ArgsEcho model = ArgsEcho.parse(args).model();
        return model;
    }

    @Override
    public Stage newStage(String... args) {
        throw new UnsupportedOperationException();
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
