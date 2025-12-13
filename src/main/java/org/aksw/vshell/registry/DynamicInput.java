package org.aksw.vshell.registry;

import java.io.IOException;
import java.nio.file.Path;

public interface DynamicInput
    extends Input
{
    // boolean hasPipe();

    /** Return a pipe. It may be created on-demand, hence the IOException. */
    // PosixPipe getPipe() throws IOException;
    boolean hasFile();
    Path getFile() throws IOException;
}
