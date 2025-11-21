package org.aksw.shellgebra.exec.graph;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

public interface FdResource extends AutoCloseable
{
    public record FdResourcePath(Path path) implements FdResource {
        @Override
        public void close() throws IOException {
            // no op
        }
    }

    public record FdResourceInputStream(InputStream inputStream) implements FdResource {
        @Override
        public void close() throws IOException {
            inputStream.close();
        }
    }

    public record FdResourceOutputStream(OutputStream outputStream) implements FdResource {
        @Override
        public void close() throws IOException {
            outputStream.close();
        }
    }

    public static FdResource of(InputStream is) { return new FdResourceInputStream(is); }
    public static FdResource of(OutputStream os) { return new FdResourceOutputStream(os); }
    public static FdResource of(Path path) { return new FdResourcePath(path); }
}
