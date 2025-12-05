package org.aksw.shellgebra.exec.graph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Objects;

import org.newsclub.net.unix.FileDescriptorCast;

import jnr.posix.POSIX;
import jnr.posix.POSIXFactory;

/**
 * Anonymous POSIX pipe with Java stream wrappers and /proc path helper.
 *
 * Linux-only if you use getReadEndProcPath().
 */
public final class PosixPipe implements Closeable {
    private final POSIX posix;
    private final int readFd;
    private final int writeFd;

    private final FileDescriptor readFdObj;
    private final FileDescriptor writeFdObj;

    private final FileInputStream in;
    private final FileOutputStream out;

    private BufferedReader reader;
    private Charset readerCharset;

    private PrintStream printer;
    private BufferedWriter writer;
    private Charset writerCharset;

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

    public InputStream getInputStream() {
        return in;
    }

    public OutputStream getOutputStream() {
        return out;
    }

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

    /**
     * Linux-only: path that other processes can open to attach to the pipe read end.
     * Think: cat $(pipe.getReadEndProcPath())
     */
    public Path getReadEndProcPath() {
        return procPath(readFd);
    }

    public File getReadEndProcFile() {
        return procPath(readFd).toFile();
    }

    public Path getWriteEndProcPath() {
        return procPath(writeFd);
    }

    public File getWriteEndProcFile() {
        return procPath(writeFd).toFile();
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

    /*
     * Convenience methods below.
     */

    public final PrintStream printer() {
        return printer(Charset.defaultCharset());
    }

    public final PrintStream printer(Charset charset) {
        Objects.requireNonNull(charset, "charset");
        synchronized (this) {
            if (printer == null) {
                if (writer != null) {
                    throw new IllegalStateException("Cannot create PrintStream because a BufferedWriter was already created with charset: " + writerCharset);
                }

                writerCharset = charset;
                printer = new PrintStream(getOutputStream(), true, charset);
            } else {
                if (!writerCharset.equals(charset)) {
                    throw new IllegalStateException("BufferedWriter was created with charset: " + writerCharset);
                }
            }
            return printer;
        }
    }

    public final BufferedWriter writer() {
        return writer(Charset.defaultCharset());
    }

    public final BufferedWriter writer(Charset charset) {
        Objects.requireNonNull(charset, "charset");
        synchronized (this) {
            if (writer == null) {
                if (printer != null) {
                    throw new IllegalStateException("Cannot create BufferedWriter because a PrintStream was already created with charset: " + writerCharset);
                }

                writerCharset = charset;
                writer = new BufferedWriter(new OutputStreamWriter(getOutputStream(), charset));
            } else {
                if (!writerCharset.equals(charset))
                    throw new IllegalStateException("BufferedWriter was created with charset: " + writerCharset);
            }
            return writer;
        }
    }

    public final BufferedReader reader() {
        return reader(Charset.defaultCharset());
    }

    public final BufferedReader reader(Charset charset) {
        Objects.requireNonNull(charset, "charset");
        synchronized (this) {
            if (reader == null) {
                readerCharset = charset;
                reader = new BufferedReader(new InputStreamReader(getInputStream(), charset));
            } else {
                if (!readerCharset.equals(charset))
                    throw new IllegalStateException("BufferedReader was created with charset: " + readerCharset);
            }
            return reader;
        }
    }
}
