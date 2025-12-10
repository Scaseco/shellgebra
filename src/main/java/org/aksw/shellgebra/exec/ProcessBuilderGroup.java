package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.aksw.vshell.registry.ProcessOverCompletableFuture;

public class ProcessBuilderGroup
    extends ProcessBuilderCompound<ProcessBuilderGroup>
{
    public static ProcessBuilderGroup of(IProcessBuilderCore<?> ... processBuilders) {
        return new ProcessBuilderGroup().processBuilders(processBuilders);
    }

    @Override
    protected ProcessBuilderGroup cloneActual() {
        return new ProcessBuilderGroup();
    }

    @Override
    public Process start(ProcessRunner executor) throws IOException {
        List<? extends IProcessBuilderCore<?>> pbs = copyProcessBuilders();
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> run(executor, pbs));
        return new ProcessOverCompletableFuture(future);
    }

    private static int run(ProcessRunner runner, Collection<? extends IProcessBuilderCore<?>> processBuilders) {
//        ExecutorService executor = Executors.newCachedThreadPool();
        Set<Process> runningProcesses = Collections.synchronizedSet(new LinkedHashSet<>(processBuilders.size()));

        int result = 0;
        for (IProcessBuilderCore<?> pb : processBuilders) {
            Process process;
            try {
                process = pb.start(runner);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            runningProcesses.add(process);
            int exitValue;
            try {
                exitValue = process.waitFor();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            result = exitValue;
            runningProcesses.remove(process);
        }
        return result;
    }
}
