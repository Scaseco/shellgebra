package org.aksw.vshell.registry;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.aksw.commons.util.docker.Argv;
import org.aksw.shellgebra.exec.Stage;

public class JvmCommandCat
    implements JvmCommand
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
    public int run(JvmExecCxt cxt, Argv argv) {
        ArgsCat model;

        int exitValue = 0;
        try {
            model = parseArgs(argv.newArgs());
        } catch (Exception e) {
            e.printStackTrace(cxt.err());
            exitValue = 2;
            return exitValue;
        }

        List<String> names = model.getFileNames().isEmpty()
            ? List.of("-")
            : model.getFileNames();

        for (String name : names) {
            try {
                if (name.equals("-")) {
                    cxt.in().transferTo(cxt.out());
                } else {
                    Path path = Path.of(name);
                    try (InputStream in = Files.newInputStream(path)) {
                        in.transferTo(cxt.out());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(cxt.err());
                exitValue = 1;
                break;
            }
        }
        return exitValue;
    }
}
