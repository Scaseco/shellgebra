package org.aksw.vshell.registry;

import java.io.IOException;

import org.aksw.shellgebra.exec.ProcessBuilderBase;
import org.aksw.shellgebra.exec.graph.ProcessRunner;

public class ProcessBuilderNative
    extends ProcessBuilderBase<ProcessBuilderNative>
{
    public static ProcessBuilderNative of(String ...command) {
        return new ProcessBuilderNative().command(command);
    }

    @Override
    public Process start(ProcessRunner executor) throws IOException {
        ProcessBuilder pb = new ProcessBuilder();

        pb.command(this.command());
        pb.environment().putAll(this.environment());
        if (this.directory() != null) {
            pb.directory(this.directory().toFile());
        }

        pb = executor.configure(pb);

        return pb.start();
    }

    @Override
    protected ProcessBuilderNative cloneActual() {
        return new ProcessBuilderNative();
    }
}
