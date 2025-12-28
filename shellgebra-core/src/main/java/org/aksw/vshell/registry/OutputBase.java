package org.aksw.vshell.registry;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Objects;

public abstract class OutputBase
    implements Output
{
    private OutputStream outputStream;

    private PrintStream printer;
    private BufferedWriter writer = null;
    private Charset writerCharset = null;

    protected OutputBase(OutputStream outputStream) {
        super();
        this.outputStream = outputStream;
    }

    protected abstract OutputStream openOutputStream() throws IOException;

    @Override
    public Charset getWriterCharset() {
        return writerCharset;
    }

    @Override
    public boolean hasWriter() {
        return writer != null;
    }

    @Override
    public boolean hasPrinter() {
        return printer != null;
    }

    @Override
    public OutputStream outputStream() {
        synchronized (this) {
            if (outputStream == null) {
                try {
                    outputStream = openOutputStream(); // Files.newOutputStream(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return outputStream;
    }

    @Override
    public final PrintStream printStream() {
        return printStream(Charset.defaultCharset());
    }

    @Override
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

    @Override
    public final BufferedWriter writer() {
        return writer(Charset.defaultCharset());
    }

    @Override
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
    public final void flush() throws IOException {
        if (printer != null) {
            printer.flush();
        } else if (writer != null) {
            writer.flush();
        } else if (outputStream != null) {
            outputStream.flush();
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
