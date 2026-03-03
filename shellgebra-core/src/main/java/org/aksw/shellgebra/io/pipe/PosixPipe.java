package org.aksw.shellgebra.io.pipe;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.aksw.vshell.registry.DynamicInput;
import org.aksw.vshell.registry.DynamicOutput;
import org.newsclub.net.unix.FileDescriptorCast;

import jnr.posix.POSIX;
import jnr.posix.POSIXFactory;

/**
 * Anonymous POSIX pipe with Java stream wrappers and /proc path helper.
 *
 * Linux-only if you use getReadEndProcPath().
 */
public final class PosixPipe
    extends PipeBase
    implements DynamicPipe
{
    private final POSIX posix;
    private final int readFd;
    private final int writeFd;

    private final FileDescriptor readFdObj;
    private final FileDescriptor writeFdObj;

    final FileInputStream in;
    final FileOutputStream out;

    private DynamicInput input;
    private DynamicOutput output;

    @Override
    public DynamicInput input() {
        if (input == null) {
            synchronized (this) {
                if (input == null) {
                    input = new DynamicInputFromPosixPipe(this);
                }
            }
        }
        return input;
    }

    @Override
    public DynamicOutput output() {
        if (output == null) {
            synchronized (this) {
                if (output == null) {
                    output = new DynamicOutputFromPosixPipe(this);
                }
            }
        }
        return output;
    }

    private PosixPipe(POSIX posix,
                      int readFd,
                      int writeFd,
                      FileDescriptor readFdObj,
                      FileDescriptor writeFdObj,
                      FileInputStream in,
                      FileOutputStream out) {
        this.posix = posix;
        this.readFd = readFd;
        this.writeFd = writeFd;
        this.readFdObj = readFdObj;
        this.writeFdObj = writeFdObj;
        this.in = in;
        this.out = out;
    }

    /**
     * Create a new anonymous pipe (readFd, writeFd).
     */
    public static PosixPipe open() throws IOException {
        POSIX posix = POSIXFactory.getPOSIX();
        int[] fds = new int[2];

        int rc = posix.pipe(fds);
        if (rc != 0) {
            int errno = posix.errno();
            throw new IOException("pipe() failed, rc=" + rc + ", errno=" + errno);
        }

        int readFd = fds[0];
        int writeFd = fds[1];

        // Wrap native fds as FileDescriptor (junixsocket)
        FileDescriptor readFdObj =
                FileDescriptorCast.unsafeUsing(readFd).getFileDescriptor();
        FileDescriptor writeFdObj =
                FileDescriptorCast.unsafeUsing(writeFd).getFileDescriptor();

        FileInputStream in = new FileInputStream(readFdObj);
        FileOutputStream out = new FileOutputStream(writeFdObj);

        return new PosixPipe(posix, readFd, writeFd, readFdObj, writeFdObj, in, out);
    }

//    @Override
//    public InputStream getInputStream() {
//        return in;
//    }
//
//    @Override
//    public OutputStream getOutputStream() {
//        return out;
//    }

    public int getReadFd() {
        return readFd;
    }

    public int getWriteFd() {
        return writeFd;
    }

    public static Path procPath(int fd) {
        long pid = ProcessHandle.current().pid();
        return Path.of("/proc/", Long.toString(pid),"fd", Integer.toString(fd));
    }

    public FileDescriptor getReadFileDescriptor() {
        return readFdObj;
    }

    public FileDescriptor getWriteFileDescriptor() {
        return writeFdObj;
    }

    /**
     * Linux-only: path that other processes can open to attach to the pipe read end.
     * Think: cat $(pipe.getReadEndProcPath())
     */
    @Override
    public Path getReadEndProcPath() {
        return procPath(readFd);
    }

    @Override
    public File getReadEndProcFile() {
        return procPath(readFd).toFile();
    }

    @Override
    public Path getWriteEndProcPath() {
        return procPath(writeFd);
    }

    @Override
    public File getWriteEndProcFile() {
        return procPath(writeFd).toFile();
    }

    @Override
    public String toString() {
        return "(PosixPipe readFd: " + readFd + " writeFd: " + writeFd + ")";
    }

    /**
     * Close both ends of the pipe.
     *
     * We let the Java streams own the close(); they will close the underlying fds.
     */
    @Override
    public void close() throws IOException {
        IOException first = null;
        try {
            in.close();
        } catch (IOException e) {
            first = e;
        }
        try {
            out.close();
        } catch (IOException e) {
            if (first == null) {
                first = e;
            }
        }
        if (first != null) {
            throw first;
        }
    }

    public static boolean isAnonymousProcPipe(Path p) throws IOException {
        // Only meaningful on Linux procfs /proc/<pid>/fd/N

        // if (p.startsWith("/proc") && p.toString().contains("/fd/")) return false;
        if (!p.startsWith("/proc")) return false;
        if (!Files.isSymbolicLink(p)) return false;

        Path target = Files.readSymbolicLink(p);
        String s = target.toString();
        // return s.startsWith("pipe:[") || s.startsWith("socket:[") || s.startsWith("anon_inode:[");
        return s.matches("^(pipe|socket|anon_inode):\\[.*\\]$");
    }
}
