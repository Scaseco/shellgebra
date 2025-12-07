package org.aksw.vshell.registry;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class FileInputSource
    implements Closeable
{
    private Path path;
    private InputStream inputStream;
    private BufferedReader reader;
    private Charset readerCharset;

    protected FileInputSource(Path path, InputStream inputStream) {
        super();
        this.path = path;
        this.inputStream = inputStream;
    }

    public static FileInputSource of(Path path, InputStream inputStream) {
        return new FileInputSource(path, inputStream);
    }

    public static FileInputSource of(Path path) {
        return of(path, null);
    }

    public static FileInputSource of(File file) {
        return of(file.toPath());
    }

    public Path getPath() {
        return path;
    }

    public File getFile() {
        return path.toFile();
    }

    public InputStream inputStream() {
        synchronized (this) {
            if (inputStream == null) {
                try {
                    inputStream = Files.newInputStream(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return inputStream;
    }

    public final BufferedReader reader() {
        return reader(Charset.defaultCharset());
    }

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
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        } else if (inputStream != null) {
            inputStream.close();
        }
    }
}
