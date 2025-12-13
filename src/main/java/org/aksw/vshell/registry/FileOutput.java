package org.aksw.vshell.registry;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileOutput
    extends OutputBase
    implements DynamicOutput
{
    private Path path;

    protected FileOutput(Path path, OutputStream outputStream) {
        super(outputStream);
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
    protected OutputStream openOutputStream() throws IOException {
        return Files.newOutputStream(path);
    }

    public static FileOutput of(Path path, OutputStream outputStream) {
        return new FileOutput(path, outputStream);
    }

    public static FileOutput of(Path path) {
        return of(path, null);
    }

    public static FileOutput of(File file) {
        return of(file.toPath());
    }

    public Path getPath() {
        return path;
    }
}
