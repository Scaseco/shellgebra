package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.nio.file.Path;

public interface PathLifeCycle {
    default void beforeExec(Path path) throws IOException {}
    default void afterExec(Path path) throws IOException {}
}
