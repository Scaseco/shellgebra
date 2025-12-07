package org.aksw.shellgebra.exec;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.aksw.shellgebra.exec.graph.PathResource;

public class ProcessPipeline
    extends Process
{
    private List<Process> processes;
    private List<PathResource> pipes;
    private CompletableFuture<?> future;

    public ProcessPipeline(List<Process> processes, List<PathResource> pipes) {
        super();
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

    // TODO Wire up
    @Override
    public OutputStream getOutputStream() {
        return null;
    }

    // TODO Wire up
    @Override
    public InputStream getInputStream() {
        return null;
    }

    // TODO Wire up
    @Override
    public InputStream getErrorStream() {
        return null;
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
