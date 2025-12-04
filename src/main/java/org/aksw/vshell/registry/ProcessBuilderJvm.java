package org.aksw.vshell.registry;

import java.io.IOException;

import org.aksw.shellgebra.exec.ProcessBuilderBase;
import org.aksw.shellgebra.exec.graph.ProcessRunner;

public class ProcessBuilderJvm
    extends ProcessBuilderBase<ProcessBuilderJvm>
{
    public ProcessBuilderJvm() {
        super();
    }

    public static ProcessBuilderJvm of(String ...argv) {
        return new ProcessBuilderJvm().command(argv);
    }

    @Override
    public Process start(ProcessRunner executor) throws IOException {
        // FIXME Still undecided whether the runner or the builder is the master.
        return executor.startJvm(this);
    }
}
