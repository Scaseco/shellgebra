package org.aksw.shellgebra.exec.graph;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public interface FdResource extends AutoCloseable
{
    // XXX Should FdResource allow for a generic list of dependent FdResources?
    //     E.g. A computation resource depending on a list of inputs.

    default InputStream asInputStream() {
        throw new RuntimeException("not an input stream");
    }

    default OutputStream asOutputStream() {
        throw new RuntimeException("not an uutput stream");
    }

    @Override
    void close() throws IOException;

    public record FdResourcePath(Path path) implements FdResource {
        @Override
        public void close() throws IOException {
            // no op
        }
    }

    public record FdResourceInputStream(InputStream inputStream, FileDescription<FdResourcePath> base) implements FdResource {
        @Override
        public InputStream asInputStream() {
            return inputStream;
        }

        @Override
        public void close() throws IOException {
            try {
                inputStream.close();
            } finally {
                if (base != null) {
                    base.close();
                }
            }
        }
    }

    public record FdResourceOutputStream(OutputStream outputStream, FileDescription<FdResourcePath> base) implements FdResource {
        @Override
        public OutputStream asOutputStream() {
            return outputStream;
        }

        @Override
        public void close() throws IOException {
            try {
                outputStream.close();
            } finally {
                if (base != null) {
                    base.close();
                }
            }
        }
    }

    /**
     * Try to cast the file description to one backed by a path. Returns null if this is not the case.
     * Only attempts a cast - reference counts remain unaffected.
     */
    @SuppressWarnings("unchecked")
    public static FileDescription<FdResourcePath> castAsFdPath(FileDescription<FdResource> fd) {
        if (fd.get() instanceof FdResourcePath) {
            return (FileDescription<FdResourcePath>)((FileDescription<?>)fd);
        }
        return null;
    }

    public static FdResourceInputStream openRead(FileDescription<FdResourcePath> fd) throws IOException {
        @SuppressWarnings("resource")
        FileDescription<FdResourcePath> dup = fd.checkedDup();
        Path path = dup.get().path();
        InputStream is;
        try {
            is = Files.newInputStream(path);
        } catch (IOException e) {
            dup.close();
            throw new IOException(e);
        }
        return new FdResourceInputStream(is, dup);
    }

    public static FdResource openWrite(FileDescription<FdResourcePath> fd) throws IOException {
        return openWriteInternal(fd);
    }

    public static FdResource openAppend(FileDescription<FdResourcePath> fd) throws IOException {
        return openWriteInternal(fd, StandardOpenOption.APPEND);
    }

    private static FdResource openWriteInternal(FileDescription<FdResourcePath> fd, OpenOption... options) throws IOException {
        @SuppressWarnings("resource")
        FileDescription<FdResourcePath> dup = fd.checkedDup();
        Path path = dup.get().path();
        OutputStream os;
        try {
            os = Files.newOutputStream(path, options);
        } catch (IOException e) {
            dup.close();
            throw new IOException(e);
        }
        return new FdResourceOutputStream(os, dup);
    }

    public static FdResource of(InputStream is) { return new FdResourceInputStream(is, null); }
    public static FdResource of(OutputStream os) { return new FdResourceOutputStream(os, null); }
    public static FdResource of(Path path) { return new FdResourcePath(path); }
}
