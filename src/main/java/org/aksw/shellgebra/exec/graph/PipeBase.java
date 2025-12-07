package org.aksw.shellgebra.exec.graph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Objects;

public abstract class PipeBase {
    private PrintStream printer;
    private BufferedWriter writer;
    private Charset writerCharset;

    private BufferedReader reader;
    private Charset readerCharset;

    protected abstract OutputStream getOutputStream();
    protected abstract InputStream getInputStream();

    /*
     * Convenience methods below, inspired by ProcessBuilder from Java 17+.
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
