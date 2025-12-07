package org.aksw.vshell.registry;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class FileOutputTarget
    implements Closeable
{
    private Path path;
    private OutputStream outputStream;

    private PrintStream printer;
    private BufferedWriter writer;
    private Charset writerCharset;

    protected FileOutputTarget(Path path, OutputStream outputStream) {
        super();
        this.path = path;
        this.outputStream = outputStream;
    }

    public static FileOutputTarget of(Path path, OutputStream outputStream) {
        return new FileOutputTarget(path, outputStream);
    }

    public static FileOutputTarget of(Path path) {
        return of(path, null);
    }

    public static FileOutputTarget of(File file) {
        return of(file.toPath());
    }

    public Path getPath() {
        return path;
    }

    public File getFile() {
        return path.toFile();
    }

    public OutputStream outputStream() {
        synchronized (this) {
            if (outputStream == null) {
                try {
                    outputStream = Files.newOutputStream(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return outputStream;
    }

    public final PrintStream printStream() {
        return printStream(Charset.defaultCharset());
    }

    public final PrintStream printStream(Charset charset) {
        Objects.requireNonNull(charset, "charset");
        synchronized (this) {
            if (printer == null) {
                if (writer != null) {
                    throw new IllegalStateException("Cannot create PrintStream because a BufferedWriter was already created with charset: " + writerCharset);
                }

                writerCharset = charset;
                printer = new PrintStream(outputStream(), true, charset);
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
                writer = new BufferedWriter(new OutputStreamWriter(outputStream(), charset));
            } else {
                if (!writerCharset.equals(charset))
                    throw new IllegalStateException("BufferedWriter was created with charset: " + writerCharset);
            }
            return writer;
        }
    }

    @Override
    public void close() throws IOException {
        if (printer != null) {
            printer.close();
        } else if (writer != null) {
            writer.close();
        } else if (outputStream != null) {
            outputStream.close();
        }
    }
}
