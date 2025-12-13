package org.aksw.vshell.registry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileInput
    extends InputBase
    implements DynamicInput
{
    private Path path;

    /** InputStream will be owned (and closed) by this instance. */
    protected FileInput(Path path, InputStream inputStream) {
        super(inputStream);
        this.path = path;
    }

    @Override
    public boolean hasFile() {
        return true;
    }

    @Override
    public Path getFile() {
        return path;
    }

    @Override
    protected InputStream openInputStream() throws IOException {
        return Files.newInputStream(path);
    }

    public static FileInput of(Path path, InputStream inputStream) {
        return new FileInput(path, inputStream);
    }

    public static FileInput of(Path path) {
        return of(path, null);
    }

    public static FileInput of(File file) {
        return of(file.toPath());
    }

    public Path getPath() {
        return path;
    }
}
