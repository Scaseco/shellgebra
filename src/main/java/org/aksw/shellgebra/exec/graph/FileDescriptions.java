package org.aksw.shellgebra.exec.graph;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

public class FileDescriptions {
    public static FileDescription<FdResource> of(OutputStream os) {
        return FileDescription.auto(FdResource.of(os));
    }

    public static FileDescription<FdResource> of(InputStream is) {
        return FileDescription.auto(FdResource.of(is));
    }

    public static FileDescription<FdResource> of(Path path) {
        return FileDescription.auto(FdResource.of(path));
    }
}
