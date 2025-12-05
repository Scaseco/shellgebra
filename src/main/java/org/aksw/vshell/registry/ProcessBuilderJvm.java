package org.aksw.vshell.registry;

import java.io.IOException;

import org.aksw.commons.util.docker.Argv;
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
//        Argv a = Argv.of(command());
//        String c = a.command();
//        JvmCommand cmd = executor.getJvmCmdRegistry().get(c)
//                .orElseThrow(() -> new RuntimeException("Command not found: " + command));
//        Process process = ProcessOverCompletableFuture.of(() -> {
//            int exitValue = cmd.run(executor, a);
//            return exitValue;
//        });
//        return process;

        // FIXME Still undecided whether the runner or the builder is the master.
        return executor.startJvm(this);
    }

    @Override
    protected ProcessBuilderJvm cloneActual() {
        return new ProcessBuilderJvm();
    }
}
