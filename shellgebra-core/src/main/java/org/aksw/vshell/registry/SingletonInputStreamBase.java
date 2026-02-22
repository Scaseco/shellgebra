package org.aksw.vshell.registry;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public abstract class SingletonInputStreamBase
    implements SingletonInputStream
{
    private InputStream inputStream;

    /** Create an instance of this class with a preset InputStream instance. */
    public SingletonInputStreamBase(InputStream inputStream) {
        super();
        this.inputStream = inputStream;
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
    public void close() throws IOException {
        IOUtils.close(inputStream);
    }
}
