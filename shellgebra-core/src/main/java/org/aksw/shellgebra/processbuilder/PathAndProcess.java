package org.aksw.shellgebra.processbuilder;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;

// Perhaps use FileWriter abstraction?
public record PathAndProcess(Path path, Process process, Closeable closeAction) implements Closeable {
    public PathAndProcess(Path path, Process process) {
        this(path, process, null);
    }

    @Override
    public void close() throws IOException {
        if (process != null) {
            process.destroy();
        }

        if (closeAction != null) {
            closeAction.close();
        }
    }
}
