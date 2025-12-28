package org.aksw.vshell.registry;

import java.io.IOException;
import java.nio.file.Path;

public interface DynamicOutput
    extends Output
{
    boolean hasFile();

    /** Return a pipe. It may be created on-demand, hence the IOException. */
    Path getFile() throws IOException;
}
