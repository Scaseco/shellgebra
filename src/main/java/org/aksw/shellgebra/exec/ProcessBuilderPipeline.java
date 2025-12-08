package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectJava;
import org.aksw.shellgebra.exec.graph.PathResource;
import org.aksw.shellgebra.exec.graph.ProcessRunner;

public class ProcessBuilderPipeline
    extends ProcessBuilderBase<ProcessBuilderPipeline>
{
    private List<IProcessBuilder<?>> processBuilders;

    public ProcessBuilderPipeline() {
        super();
        this.processBuilders = List.of();
    }

    public ProcessBuilderPipeline processBuilders(IProcessBuilder<?>... processBuilders) {
        processBuilders(List.of(processBuilders));
        return self();
    }

    public ProcessBuilderPipeline processBuilders(List<IProcessBuilder<?>> processBuilders) {
        this.processBuilders = List.copyOf(processBuilders);
        return self();
    }

    public static ProcessBuilderPipeline of(IProcessBuilder<?> ... processBuilders) {
        return new ProcessBuilderPipeline().processBuilders(processBuilders);
    }

    @Override
    public Process start(ProcessRunner executor) throws IOException {
        int n = processBuilders.size();
        if (n == 0) {
            throw new IllegalStateException("Pipeline must have at least one member.");
        }

        PathResource priorPath = null;

        List<CompletableFuture<Process>> processFutures = new ArrayList<>(n);
        // List<Process> processes = new ArrayList<>(n);
        List<PathResource> pipes = new ArrayList<>(n - 1);
        PathLifeCycle lifeCycle = PathLifeCycles.deleteAfterExec(PathLifeCycles.namedPipe());

        // TODO Improve error handling: Always shut down executor service, deal with process startup failure.
        ExecutorService executorService = Executors.newCachedThreadPool();

        for (int i = 0; i < n; ++i) {
            boolean isLast = i == n - 1;
            IProcessBuilder<?> current = processBuilders.get(i);
            IProcessBuilder<?> tmp = current.clone();

            // Set up redirect input.
            if (priorPath == null) {
                // FIXME Life cycles are currently not managed correctly:
                //   Redirect.INHERIT does NOT close the corresponding input stream whereas using a
                //   redirect.FILE does close the pipe.
                tmp.redirectInput(new JRedirectJava(Redirect.INHERIT));
                // tmp.redirectInput(new JRedirectJava(Redirect.from(executor.inputPipe().toFile())));
            } else {
                tmp.redirectInput(new JRedirectJava(Redirect.from(priorPath.getPath().toFile())));
            }

            // Set up redirect output.
            if (isLast) {
                // tmp.redirectOutput(new JRedirectJava(Redirect.to(executor.outputPipe().toFile())));
                tmp.redirectOutput(new JRedirectJava(Redirect.INHERIT));
            } else {
                Path namedPipePath = SysRuntime.newNamedPipePath();
                PathResource thisPath = new PathResource(namedPipePath, lifeCycle);
                thisPath.open();
                tmp.redirectOutput(new JRedirectJava(Redirect.to(thisPath.getPath().toFile())));
                pipes.add(thisPath);
                priorPath = thisPath;
            }

            // Set up redirect error.
            tmp.redirectError(new JRedirectJava(Redirect.INHERIT));
            // tmp.redirectError(new JRedirectJava(Redirect.to(executor.errorPipe().toFile())));

            // Start.
            // Note: If named pipes are involved, then a process cannot start before the target process has been connected.

            CompletableFuture<Process> processFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return tmp.start(executor);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, executorService);
            processFutures.add(processFuture);
            // Process process = tmp.start(executor);
            // processes.add(process);
        }
        List<Process> processes = CompletableFuture.allOf(processFutures.toArray(CompletableFuture[]::new)).thenApply(v -> {
            return processFutures.stream().map(CompletableFuture::join).toList();
        }).join();
        executorService.shutdown();

        return new ProcessPipeline(processes, pipes);
    }

    @Override
    protected ProcessBuilderPipeline cloneActual() {
        ProcessBuilderPipeline result = new ProcessBuilderPipeline();
        return result;
    }
}
