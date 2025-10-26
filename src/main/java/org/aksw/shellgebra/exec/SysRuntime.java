package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.transform.CmdString;

public interface SysRuntime {
    String which(String cmdName) throws IOException, InterruptedException;

    /** Quote a filename for use as an argument.*/
    String quoteFileArgument(String fileName);

    CmdString compileString(CmdOp op);
    String[] compileCommand(CmdOp op);

    CmdStrOps getStrOps();

    /** Create a named pipe at the given path. */
    void createNamedPipe(Path path) throws IOException;

    /**
     * Resolve the first argument of the array against {@link #which(String)}.
     * Returned array is always a copy.
     */
    default String[] resolveCommand(String... argv) throws IOException, InterruptedException {
        Objects.requireNonNull(argv);
        if (argv.length == 0) {
            throw new IllegalArgumentException("Command must not be an empty array.");
        }

        String cmdName = argv[0];
        String resolvedName = which(cmdName);
        if (resolvedName == null) {
            throw new RuntimeException("Command not found: " + cmdName);
        }

        String[] result = Arrays.copyOf(argv, argv.length);
        result[0] = resolvedName;
        return result;
    }

    public static CmdString toString(CmdOp cmdOp) {
        SysRuntime runtime = SysRuntimeImpl.forCurrentOs();
        CmdString cmdString = runtime.compileString(cmdOp);
        return cmdString;
    }
}
