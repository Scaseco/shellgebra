package org.aksw.shellgebra.processbuilder;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.aksw.shellgebra.exec.graph.PathResource;
import org.aksw.vshell.registry.ProcessBase;

public class ProcessPipeline
    extends ProcessBase
{
    private List<Process> processes;
    private List<PathResource> pipes;
    private CompletableFuture<?> future;

    public ProcessPipeline(List<Process> processes, List<PathResource> pipes, OutboundIo outboundIo) {
        super(outboundIo);
        this.processes = processes;
        this.pipes = pipes;
        List<CompletableFuture<Process>> futures = processes.stream().map(Process::onExit).toList();
        future = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).whenComplete((x, e) -> {
            for (PathResource pipe : this.pipes) {
                try {
                    pipe.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    @Override
    public int waitFor() throws InterruptedException {
        try {
            future.get();
        } catch ( ExecutionException e) {
            throw new RuntimeException(e);
        }
        return processes.get(processes.size() - 1).exitValue();
    }

    @Override
    public int exitValue() {
        return processes.get(processes.size() - 1).exitValue();
    }

    @Override
    public void destroy() {
        processes.forEach(Process::destroy);
    }
}
