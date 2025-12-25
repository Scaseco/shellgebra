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

    public static ProcessBuilderGroup of(List<? extends IProcessBuilderCore<?>> processBuilders) {
        return new ProcessBuilderGroup().processBuilders(processBuilders);
    }

    public ProcessBuilderGroup() {
        super();
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

    private int run(ProcessRunner runner, Collection<? extends IProcessBuilderCore<?>> processBuilders) {
        Set<Process> runningProcesses = Collections.synchronizedSet(new LinkedHashSet<>(processBuilders.size()));
        int result = 0;
        for (IProcessBuilderCore<?> pb : processBuilders) {
            pb.redirectInput(redirectInput());
            pb.redirectOutput(redirectOutput());
            pb.redirectError(redirectError());

            Process process;
            try {
                System.out.println("Process started from: " + pb);
                process = pb.start(runner);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            runningProcesses.add(process);
            int exitValue;
            try {
                exitValue = process.waitFor();
                System.out.println("Process finished from: " + pb);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            result = exitValue;
            runningProcesses.remove(process);
        }
        return result;
    }

    @Override
    public boolean supportsAnonPipeRead() {
        boolean result = processBuilders().stream().allMatch(IProcessBuilderCore::supportsAnonPipeRead);
        return result;
    }

    @Override
    public boolean supportsAnonPipeWrite() {
        boolean result = processBuilders().stream().allMatch(IProcessBuilderCore::supportsAnonPipeWrite);
        return result;
    }

    /** Groups only support direct named pipes if there is only a single member that accepts a direct named pipe. */
    @Override
    public boolean supportsDirectNamedPipe() {
        boolean result = processBuilders().stream().filter(IProcessBuilderCore::supportsDirectNamedPipe).count() <= 1;
        return result;
    }

    @Override
    public boolean accessesStdIn() {
        boolean result = processBuilders().stream().anyMatch(IProcessBuilderCore::accessesStdIn);
        return result;
    }
}
