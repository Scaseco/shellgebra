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
import org.aksw.shellgebra.exec.graph.PosixPipe;
import org.aksw.shellgebra.exec.graph.ProcessRunner;

public class ProcessBuilderPipeline
    extends ProcessBuilderCompound<ProcessBuilderPipeline>
{
    public ProcessBuilderPipeline() {
        super();
    }

    @Override
    public boolean supportsAnonPipeRead() {
        List<? extends IProcessBuilderCore<?>> pbs = processBuilders();
        IProcessBuilderCore<?> first = pbs.get(0);
        return first.supportsAnonPipeRead();
    }

    @Override
    public boolean supportsDirectNamedPipe() {
        List<? extends IProcessBuilderCore<?>> pbs = processBuilders();
        IProcessBuilderCore<?> first = pbs.get(0);
        return first.supportsDirectNamedPipe();
    }

    @Override
    public boolean supportsAnonPipeWrite() {
        List<? extends IProcessBuilderCore<?>> pbs = processBuilders();
        IProcessBuilderCore<?> last = pbs.get(pbs.size() - 1);
        return last.supportsAnonPipeWrite();
    }

    public static ProcessBuilderPipeline of(IProcessBuilderCore<?> ... processBuilders) {
        return new ProcessBuilderPipeline().processBuilders(processBuilders);
    }

    public static ProcessBuilderPipeline of(List<? extends IProcessBuilderCore<?>> processBuilders) {
        return new ProcessBuilderPipeline().processBuilders(processBuilders);
    }

    @Override
    protected ProcessBuilderPipeline cloneActual() {
        return new ProcessBuilderPipeline();
    }

    @Override
    public Process start(ProcessRunner executor) throws IOException {
        List<? extends IProcessBuilderCore<?>> pbs = copyProcessBuilders();

        int n = pbs.size();
        if (n == 0) {
            throw new IllegalStateException("Pipeline must have at least one member.");
        }

        Path priorPath = null;
        IProcessBuilderCore<?> priorBuilder = null;

        List<CompletableFuture<Process>> processFutures = new ArrayList<>(n);
        List<PathResource> pipes = new ArrayList<>(n - 1);
        PathLifeCycle namedPipeLifeCycle = PathLifeCycles.deleteAfterExec(PathLifeCycles.namedPipe());

        // TODO Improve error handling: Always shut down executor service, deal with process startup failure.
        ExecutorService executorService = Executors.newCachedThreadPool();

        for (int i = 0; i < n; ++i) {
            boolean isLast = i == n - 1;
            IProcessBuilderCore<?> currentBuilderPrototype = pbs.get(i);
            IProcessBuilderCore<?> current = currentBuilderPrototype.clone();

            IProcessBuilderCore<?> next = isLast ? null : pbs.get(i + 1);

            // Set up redirect input.
            if (priorPath == null) {
                // FIXME Life cycles are currently not managed correctly:
                //   Redirect.INHERIT does NOT close the corresponding input stream whereas using a
                //   redirect.FILE does close the pipe.
                current.redirectInput(new JRedirectJava(Redirect.INHERIT));
            } else {
                current.redirectInput(new JRedirectJava(Redirect.from(priorPath.toFile())));
            }

            // Set up redirect output.
            if (isLast) {
                current.redirectOutput(new JRedirectJava(Redirect.INHERIT));
            } else {
                // Named pipes break (due to blocking semantics) when used more than once.
                // So for groups where more than one member reads from the named pipe, we need to create an anon
                // pipe, and each respective group member will do an extra cat from it.

                // so is the flag "supportsDirectNamedPipe" - and a group returns false if more than 1 member returns true?
                boolean thisSupportsNamedPipeOutput = current.supportsDirectNamedPipe();
                @SuppressWarnings("null") // isLast == true implies nextBuilderPrototype == null
                boolean nextRequiresNamedPipeInput = next.supportsDirectNamedPipe() && !next.supportsAnonPipeRead();

                boolean thisRequiresNamedPipeOutput = current.supportsDirectNamedPipe() && !current.supportsAnonPipeWrite();
                boolean nextSupportsNamedPipeInput = next.supportsDirectNamedPipe();

                boolean useNamedPipe1 = thisRequiresNamedPipeOutput && nextSupportsNamedPipeInput;
                boolean useNamedPipe2 = thisSupportsNamedPipeOutput && nextRequiresNamedPipeInput;
                boolean useNamedPipe = useNamedPipe1 || useNamedPipe2;

                PathResource thisPath;
                if (useNamedPipe) {
                    Path namedPipePath = SysRuntime.newNamedPipePath();
                    thisPath = new PathResource(namedPipePath, namedPipeLifeCycle);
                    thisPath.open();
                    current.redirectOutput(new JRedirectJava(Redirect.to(thisPath.getPath().toFile())));
                    priorPath = thisPath.getPath();
                } else {
                    PosixPipe pipe = PosixPipe.open();
                    thisPath = new PathResource(pipe.getWriteEndProcPath(), PathLifeCycles.none());
                    current.redirectOutput(new JRedirectJava(Redirect.to(thisPath.getPath().toFile())));
                    priorPath = pipe.getReadEndProcPath();
                }
                pipes.add(thisPath);
                priorBuilder = current;
            }

            // Set up redirect error.
            current.redirectError(new JRedirectJava(Redirect.INHERIT));

            // Note: If named pipes are involved, then a process cannot start before the target process has been connected.
            CompletableFuture<Process> processFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return current.start(executor);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, executorService);
            processFutures.add(processFuture);
        }
        List<Process> processes = CompletableFuture.allOf(processFutures.toArray(CompletableFuture[]::new)).thenApply(v -> {
            return processFutures.stream().map(CompletableFuture::join).toList();
        }).join();
        executorService.shutdown();

        return new ProcessPipeline(processes, pipes);
    }
}
