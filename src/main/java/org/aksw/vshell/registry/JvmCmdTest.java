package org.aksw.vshell.registry;

import org.aksw.commons.util.docker.Argv;
import org.aksw.shellgebra.exec.Stage;

public class JvmCmdTest
    implements JvmCommand
{
    @Override
    public ArgsTest parseArgs(String... args) {
        ArgsTest model = ArgsTest.parse(args).model();
        return model;
    }

    @Override
    public Stage newStage(String... args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int run(JvmExecCxt cxt, Argv argv) {
        ArgsTest model;

        int exitValue = 0;
        try {
            model = parseArgs(argv.newArgs());
        } catch (Exception e) {
            e.printStackTrace(cxt.err().printStream());
            exitValue = 2;
            return exitValue;
        }

        String testName = model.getE();
        if (testName == null) {
            throw new RuntimeException("Only -e option supported yet.");
        }

        JvmCommandRegistry reg = cxt.context().getJvmCmdRegistry();
        boolean isPresent = reg.get(testName).isPresent();
        exitValue = isPresent ? 0 : 1;
        return exitValue;
    }
}
