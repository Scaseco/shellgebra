package org.aksw.shellgebra.exec;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.transform.CmdString;
import org.newsclub.net.unix.FileDescriptorCast;

public interface SysRuntime
    extends AutoCloseable
{
    String which(String cmdName) throws IOException, InterruptedException;
    boolean exists(String cmdName) throws IOException, InterruptedException;

    /** Quote a filename for use as an argument.*/
    String quoteFileArgument(String fileName);

    CmdString compileString(CmdOp op);
    String[] compileCommand(CmdOp op);

    CmdStrOps getStrOps();

    /** Create a named pipe at the given path. */
    void createNamedPipe(Path path) throws IOException;

    /**
     * Compile the command such that it can act as a single argument.
     * Such as {@code bash -c 'sub-command-expression'}.
     */
    default String toScriptString(CmdOp cmdOp) {
        CmdOp dummy = new CmdOpExec("/dummy", CmdArg.ofCommandSubstitution(cmdOp));
        CmdString cmdString = compileString(dummy);
        String scriptString = cmdString.cmd()[1];
        return scriptString;
    }

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

    // XXX Move to a better location?
    public static String toScriptStringX(CmdOp cmdOp) {
        SysRuntime runtime = SysRuntimeImpl.forCurrentOs();
        String scriptString = runtime.toScriptString(cmdOp);
        return scriptString;
    }

    @Override
    public void close();

    /** Returns a path such as /proc/process_id/fd/123 */
    public static Path getFdPath(FileDescriptor fd) {
        int fdVal;
        try {
            fdVal = FileDescriptorCast.using(fd).as(Integer.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        long pid = ProcessHandle.current().pid();
        Path result = Path.of("/proc", Long.toString(pid), "fd", Integer.toString(fdVal));
        return result;
    }

    /** Does not work: Unable to make field private int java.io.FileDescriptor.fd accessible: module java.base does not "opens java.io" to unnamed module @20abdeca */
    private static int extractFD(FileDescriptor fd) {
        try {
            Field field = FileDescriptor.class.getDeclaredField("fd");
            field.setAccessible(true);
            return (int)field.get(fd);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }
}
