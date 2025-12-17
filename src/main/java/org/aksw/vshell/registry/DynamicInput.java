package org.aksw.vshell.registry;

import java.io.IOException;
import java.nio.file.Path;

public interface DynamicInput
    extends Input
{
    boolean hasFile();

    /**
     * Return a file (typically a pipe).
     * Calling this function may be create the pipe on-demand, hence the IOException.
     * Use {@link #hasFile()} to check for file backing without on-demand creation.
     */
    Path getFile() throws IOException;

    // boolean hasPipe();
    // PosixPipe getPipe() throws IOException;
}
