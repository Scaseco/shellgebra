package org.aksw.vshell.registry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Objects;

public abstract class InputBase
    implements Input
{
    private InputStream inputStream;
    private BufferedReader reader = null;
    private Charset readerCharset = null;

    /** Create an instance of this class with a preset InputStream instance. */
    public InputBase(InputStream inputStream) {
        super();
        this.inputStream = inputStream;
    }

    public static Input ofNullable(InputStream inputStream) {
        Input result = (inputStream == null)
            ? null
            : new InputBase(inputStream) {
                @Override
                protected InputStream openInputStream() throws IOException {
                    throw new IllegalStateException("Should never be called because stream is set on init.");
                }
            };
        return result;
    }

    public static Input of(InputStream inputStream) {
        Objects.requireNonNull(inputStream);
        return ofNullable(inputStream);
    }

    protected abstract InputStream openInputStream() throws IOException;

    @Override
    public InputStream inputStream() {
        if (inputStream == null) {
            synchronized (this) {
                if (inputStream == null) {
                    try {
                        inputStream = openInputStream(); // Files.newInputStream(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return inputStream;
    }

    @Override
    public Charset getReaderCharset() {
        return readerCharset;
    }

    @Override
    public boolean hasReader() {
        return reader != null;
    }

    @Override
    public final BufferedReader reader() {
        return reader(Charset.defaultCharset());
    }

    @Override
    public final BufferedReader reader(Charset charset) {
        Objects.requireNonNull(charset, "charset");
        if (reader == null || readerCharset == null) {
            synchronized (this) {
                if (reader == null) {
                    readerCharset = charset;
                    reader = new BufferedReader(new InputStreamReader(inputStream(), charset));
                }
            }
        } else if (!readerCharset.equals(charset)) {
            throw new IllegalStateException("BufferedReader was created with charset: " + readerCharset);
        }
        return reader;
    }

    @Override
    public final void transferTo(Output output) throws IOException {
        if (hasReader()) {
            if (output.hasPrinter()) {
                reader().transferTo(new OutputStreamWriter(output.printStream(), output.getWriterCharset()));
            } else {
                reader().transferTo(output.writer());
            }
        } else {
            if (output.hasPrinter()) {
                reader().transferTo(new OutputStreamWriter(output.printStream(), readerCharset));
            } else if (output.hasWriter()) {
                reader().transferTo(output.writer());
            } else {
                inputStream().transferTo(output.outputStream());
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        } else if (inputStream != null) {
            inputStream.close(); // Closes the underlying input stream.
        }
    }
}
