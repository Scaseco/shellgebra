package org.aksw.vshell.registry;

import java.io.IOException;

import org.aksw.shellgebra.exec.IProcessBuilder;

public interface JvmCommandExecutor {
    IProcessBuilder<?> newProcessBuilder(String... args);

    int run(String... argv) throws IOException;
    String exec(String... argv) throws IOException;
}
