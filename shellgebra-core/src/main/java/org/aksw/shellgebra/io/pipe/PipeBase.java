package org.aksw.shellgebra.io.pipe;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

public abstract class PipeBase
    implements Pipe
{
    public InputStream inputStream() {
        return input().inputStream();
    }

    public OutputStream outputStream() {
        return output().outputStream();
    }

    /*
     * Convenience methods below, inspired by ProcessBuilder from Java 17+.
     */

    public final PrintStream printer() {
        return output().printStream();
    }

    public final PrintStream printer(Charset charset) {
        return output().printStream(charset);
    }

    public final BufferedWriter writer() {
        return output().writer();
    }

    public final BufferedWriter writer(Charset charset) {
        return output().writer(charset);
    }

    public final BufferedReader reader() {
        return input().reader();
    }

    public final BufferedReader reader(Charset charset) {
        return input().reader(charset);
    }

    // XXX Pipe.close() does not have to be called if the underlying streams have been closed.
    // Should the API contract still require a close? Or perhaps call this method "forceClose()"?
    @Override
    public void close() throws IOException {
        try {
            output().outputStream().close();
        } finally {
            input().inputStream().close();
        }
    }
}
