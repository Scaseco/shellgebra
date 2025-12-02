package org.aksw.vshell.registry;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.aksw.shellgebra.exec.Stage;
import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.aksw.vshell.shim.rdfconvert.JvmCommandBase;
import org.apache.commons.exec.ExecuteException;

public class JvmCommandCat
    extends JvmCommandBase<ArgsCat>
{
    @Override
    public ArgsCat parseArgs(String... args) {
        ArgsCat model = ArgsCat.parse(args).model();
        return model;
    }

    @Override
    public Stage newStage(String... args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void runActual(ProcessRunner cxt, ArgsCat model) throws ExecuteException {
        int exitValue = 0;
        List<String> names = model.getFileNames().isEmpty()
            ? List.of("-")
            : model.getFileNames();

        for (String name : names) {
            try {
                if (name.equals("-")) {
                    cxt.internalIn().transferTo(cxt.internalOut());
                } else {
                    Path path = Path.of(name);
                    try (InputStream in = Files.newInputStream(path)) {
                        in.transferTo(cxt.internalOut());
                        // XXX flush?
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(cxt.internalPrintErr());
                exitValue = 1;
                break;
            }
        }

        if (exitValue != 0) {
            // TODO Improve exception - should we reuse ExecuteException or roll our own?
            throw new ExecuteException("One or more arguments failed to open as files.", 1);
        }
    }
}
