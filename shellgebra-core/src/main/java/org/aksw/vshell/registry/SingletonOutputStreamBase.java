package org.aksw.vshell.registry;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

public abstract class SingletonOutputStreamBase
    implements SingletonOutputStream
{
    protected OutputStream outputStream; // private?

    protected SingletonOutputStreamBase(OutputStream outputStream) {
        super();
        this.outputStream = outputStream;
    }

    protected abstract OutputStream openOutputStream() throws IOException;

    @Override
    public OutputStream outputStream() {
        if (outputStream == null) {
            synchronized (this) {
                if (outputStream == null) {
                    try {
                        outputStream = openOutputStream(); // Files.newOutputStream(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return outputStream;
    }

    @Override
    public void close() throws IOException {
        IOUtils.close(outputStream);
    }
}
