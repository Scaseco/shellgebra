package org.aksw.vshell.registry;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.aksw.vshell.shim.rdfconvert.ArgsModular;
import org.aksw.vshell.shim.rdfconvert.JvmCommandBase;
import org.apache.commons.exec.ExecuteException;

public class JvmCommandHead
    extends JvmCommandBase<ArgsHead>
{
    @Override
    public ArgsModular<ArgsHead> parseArgs(String... args) {
        ArgsModular<ArgsHead> result = ArgsHead.parse(args);
        return result;
    }

    @Override
    public void runActual(JvmExecCxt cxt, ArgsHead model) throws ExecuteException {
        int exitValue = 0;
        List<String> names = model.getFileNames().isEmpty()
            ? List.of("-")
            : model.getFileNames();

        for (String name : names) {
            try {
                if (name.equals("-")) {
                    cxt.in().reader(StandardCharsets.UTF_8)
                        .lines()
                        .limit(model.getLines().orElse(10l))
                        .forEach(cxt.out().printStream()::println);
                } else {
                    Path path = Path.of(name);
                    try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                        try (Stream<String> stream = reader
                            .lines()
                            .limit(model.getLines().orElse(10l))) {
                            stream.forEach(cxt.out().printStream()::println);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(cxt.err().printStream());
                exitValue = 1;
                break;
            }
        }

        if (exitValue != 0) {
            // TODO Improve exception - should  we reuse ExecuteException or roll our own?
            throw new ExecuteException("One or more arguments failed to open as files.", 1);
        }
    }
}
