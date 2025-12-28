package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectJava;
import org.aksw.shellgebra.exec.graph.PathResource;
import org.aksw.shellgebra.exec.graph.PosixPipe;
import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessBuilderPipeline
    extends ProcessBuilderCompound<ProcessBuilderPipeline>
{
    private static final Logger logger = LoggerFactory.getLogger(ProcessBuilderPipeline.class);

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
    public boolean accessesStdIn() {
        List<? extends IProcessBuilderCore<?>> pbs = processBuilders();
        IProcessBuilderCore<?> first = pbs.get(0);
        return first.accessesStdIn();
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
        Callable<?> thisWriteEnd = null;
        Callable<?> nextReadEnd = null;
        Callable<?> prevReadEnd = null;

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
                    thisWriteEnd = () -> { thisPath.close(); return null; };

                    boolean doesNotAccessStdIn = !(next != null && next.accessesStdIn());

                    nextReadEnd = () -> {
                        if (doesNotAccessStdIn) {
                            try (InputStream unused = Files.newInputStream(namedPipePath)) {}
                        }
                        return null;
                    };
                } else {
                    PosixPipe pipe = PosixPipe.open();
                    thisPath = new PathResource(pipe.getWriteEndProcPath(), PathLifeCycles.none());
                    current.redirectOutput(new JRedirectJava(Redirect.to(thisPath.getPath().toFile())));
                    priorPath = pipe.getReadEndProcPath();
                    thisWriteEnd = () -> {
                        System.out.println("Closing write FD: " + pipe.getWriteFd());
                        pipe.getOutputStream().close(); return null;
                    };
                    nextReadEnd = () -> {
                        System.out.println("Closing read FD: " + pipe.getReadFd());
                        pipe.getInputStream().close(); return null;
                    };
                    logger.info("Created anonymous pipe, ReadFD=" + pipe.getReadFd() + " WriteFD=" + pipe.getWriteFd());
                }
                pipes.add(thisPath);
                priorBuilder = current;
            }

            // Set up redirect error.
            current.redirectError(new JRedirectJava(Redirect.INHERIT));
            Callable<?> thisWriteEndCloser = thisWriteEnd;
            Callable<?> thisReadEndCloser = prevReadEnd;

            // Callable<?> nextc = nextReadEnd;

            // Note: If named pipes are involved, then a process cannot start before the target process has been connected.
            Supplier<Process> processSupplier = () -> {
                try {
                    Process r = current.start(executor);
                    r.waitFor();
                    return r;
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    try {
                        if (thisReadEndCloser != null) {
                            try {
                                thisReadEndCloser.call();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    } finally {
                        if (thisWriteEndCloser != null) {
                            try {
                                thisWriteEndCloser.call();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            };
            CompletableFuture<Process> processFuture = CompletableFuture.supplyAsync(processSupplier, executorService);
            processFutures.add(processFuture);
            prevReadEnd = nextReadEnd;
        }

        List<Process> processes = CompletableFuture.allOf(processFutures.toArray(CompletableFuture[]::new)).thenApply(v -> {
            return processFutures.stream().map(CompletableFuture::join).toList();
        }).join();
        executorService.shutdown();

        return new ProcessPipeline(processes, pipes); // List.of()
    }
}
