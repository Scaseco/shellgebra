package org.aksw.shellgebra.exec;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
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

    @Override
    public void close();

    public static Path newNamedPipePath() throws IOException {
        String baseDir = System.getProperty("java.io.tmpdir");
        String fileName = "named-pipe-" + System.nanoTime();
        Path result = Path.of(baseDir).resolve(fileName);
        return result;
    }

    public static Path newNamedPipe() throws IOException {
        Path result = newNamedPipePath();
        newNamedPipe(result);
        return result;
    }

    public static void newNamedPipe(Path path) throws IOException {
        SysRuntimeImpl.forCurrentOs().createNamedPipe(path);
    }

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
