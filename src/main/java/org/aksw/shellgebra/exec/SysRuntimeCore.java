package org.aksw.shellgebra.exec;

import java.io.IOException;

public interface SysRuntimeCore
    extends AutoCloseable
{
    // TODO Abstract as Process and make other variants default implementations.
    // Process execProcess(String... argv);

    String execCmd(String... argv) throws IOException, InterruptedException;
    int runCmd(String... argv) throws IOException, InterruptedException;

    @Override
    void close();
}
