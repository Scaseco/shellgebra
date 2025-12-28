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

    public InputBase(InputStream inputStream) {
        super();
        this.inputStream = inputStream;
    }

    protected abstract InputStream openInputStream() throws IOException;

    @Override
    public Charset getReaderCharset() {
        return readerCharset;
    }

    @Override
    public boolean hasReader() {
        return reader != null;
    }

    @Override
    public InputStream inputStream() {
        synchronized (this) {
            if (inputStream == null) {
                try {
                    inputStream = openInputStream(); // Files.newInputStream(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return inputStream;
    }

    @Override
    public final BufferedReader reader() {
        return reader(Charset.defaultCharset());
    }

    @Override
    public final BufferedReader reader(Charset charset) {
        Objects.requireNonNull(charset, "charset");
        synchronized (this) {
            if (reader == null) {
                readerCharset = charset;
                reader = new BufferedReader(new InputStreamReader(inputStream(), charset));
            } else {
                if (!readerCharset.equals(charset))
                    throw new IllegalStateException("BufferedReader was created with charset: " + readerCharset);
            }
            return reader;
        }
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
            inputStream.close();
        }
    }
}
