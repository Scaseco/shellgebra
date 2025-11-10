package org.aksw.shellgebra.exec;

import java.io.Closeable;
import java.io.IOException;

public interface SysRuntimeCore
    extends Closeable
{
    // TODO Abstract as Process and make other variants default implementations.
    // Process execProcess(String... argv);

    IProcessBuilder<?> newProcessBuilder();

    String execCmd(String... argv) throws IOException, InterruptedException;
    int runCmd(String... argv) throws IOException, InterruptedException;

    @Override
    void close();
}
