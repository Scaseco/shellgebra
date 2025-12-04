package org.aksw.vshell.registry;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.aksw.shellgebra.exec.Stage;
import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.aksw.vshell.shim.rdfconvert.JvmCommandBase;
import org.apache.commons.exec.ExecuteException;

public class JvmCommandHead
    extends JvmCommandBase<ArgsHead>
{
    @Override
    public ArgsHead parseArgs(String... args) {
        ArgsHead model = ArgsHead.parse(args).model();
        return model;
    }

    @Override
    public Stage newStage(String... args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void runActual(ProcessRunner cxt, ArgsHead model) throws ExecuteException {
        int exitValue = 0;
        List<String> names = model.getFileNames().isEmpty()
            ? List.of("-")
            : model.getFileNames();

        for (String name : names) {
            try {
                if (name.equals("-")) {
                    new BufferedReader(new InputStreamReader(cxt.internalIn(), StandardCharsets.UTF_8))
                        .lines()
                        .limit(model.getLines().orElse(10l))
                        .forEach(cxt.internalPrintOut()::println);
                } else {
                    Path path = Path.of(name);
                    try (Stream<String> stream = new BufferedReader(new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8))
                        .lines()
                        .limit(model.getLines().orElse(10l))) {
                        stream.forEach(cxt.internalPrintOut()::println);
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
