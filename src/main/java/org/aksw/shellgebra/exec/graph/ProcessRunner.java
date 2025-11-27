package org.aksw.shellgebra.exec.graph;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;
import java.lang.ProcessBuilder.Redirect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.aksw.shellgebra.exec.PathLifeCycle;
import org.aksw.shellgebra.exec.PathLifeCycles;
import org.aksw.shellgebra.exec.graph.FdResource.FdResourceInputStream;
import org.aksw.shellgebra.exec.graph.FdResource.FdResourceOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process runner provides an environment for running a set of processes.
 * All processes can share the same input, output and error streams.
 *
 * The process runner manages a set of  named pipes.
 * Processes launched with this runner are configured with the appropriate pipe ends.
 */
public class ProcessRunner
    implements AutoCloseable
{
    private Logger logger = LoggerFactory.getLogger(ProcessRunner.class);

    private Path basePath;

    private PathResource fd0;
    private PathResource fd1;
    private PathResource fd2;

    private boolean fd0OverridesInherit = false;
    private boolean fd1OverridesInherit = false;
    private boolean fd2OverridesInherit = false;

    // private Thread dummyThread;
    private Runnable closeAction;


    private FdTable fdTable;

    private ExecutorService executorService;

    // private Thread inThread;
    private CompletableFuture<?> inThread;
    private Thread outThread;
    private Thread errThread;

//    private OutputStream in;
//    private InputStream out;
//    private InputStream err;

    public ProcessRunner(Path basePath, PathResource fd0, PathResource fd1, PathResource fd2,
            FdTable fdTable,
            boolean fd0OverridesInherit, boolean fd1OverridesInherit, boolean fd2OverridesInherit,
            Runnable closeAction) {
        super();
        this.basePath = basePath;
        this.executorService = Executors.newCachedThreadPool();
        this.fd0OverridesInherit = fd0OverridesInherit;
        this.fd1OverridesInherit = fd1OverridesInherit;
        this.fd2OverridesInherit = fd2OverridesInherit;

        this.fdTable = fdTable;

        this.fd0 = fd0;
        this.fd1 = fd1;
        this.fd2 = fd2;

        this.closeAction = closeAction;
    }

    public FdTable getFdTable() {
        return fdTable;
    }

    private void configureInput(Redirect redirect, PathResource fd, boolean fdOverridesInherit, Consumer<Redirect> redirectConsumer) {
        Type type = redirect.type();
        switch (type) {
        case PIPE:
            redirectConsumer.accept(Redirect.from(fd.getPath().toFile()));
            break;
        case INHERIT:
            if (fdOverridesInherit) {
                redirectConsumer.accept(Redirect.from(fd.getPath().toFile()));
            }
            break;
        default:
            // nothing to do.
        }
    }

    private void configureOutput(Redirect redirect, PathResource fd, boolean fdOverridesInherit, Consumer<Redirect> redirectConsumer) {
        Type type = redirect.type();
        switch (type) {
        case PIPE:
            redirectConsumer.accept(Redirect.to(fd.getPath().toFile()));
            break;
        case INHERIT:
            if (fdOverridesInherit) {
                redirectConsumer.accept(Redirect.to(fd.getPath().toFile()));
            }
            break;
        default:
            // nothing to do.
        }
    }

    public Thread setOutputReader(Consumer<InputStream> reader) {
        Thread thread = new Thread(() -> {
            try (InputStream in = getInputStream()) {
                reader.accept(in);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
        outThread = thread;
        return thread;
    }

    public Thread setOutputLineReaderUtf8(Consumer<String> lineCallback) {
        return setOutputLineReader(StandardCharsets.UTF_8, lineCallback);
    }

    public Thread setOutputLineReader(Charset charset, Consumer<String> lineCallback) {
        return setOutputReader(in -> readLines(in, charset, lineCallback));
    }

    public Thread setErrorReader(Consumer<InputStream> reader) {
        Thread thread = new Thread(() -> {
            try (InputStream in = getErrorStream()) {
                reader.accept(in);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
        errThread = thread;
        return thread;
    }

    public Thread setErrorLineReaderUtf8(Consumer<String> lineCallback) {
        return setErrorLineReader(StandardCharsets.UTF_8, lineCallback);
    }

    public Thread setErrorLineReader(Charset charset, Consumer<String> lineCallback) {
        return setErrorReader(in -> readLines(in, charset, lineCallback));
    }

    public Future<?> setInputGenerator(Consumer<OutputStream> inputSupplier) {
        Runnable runnable = () -> {
            try (OutputStream out = getOutputStream()) {
                inputSupplier.accept(out);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        };
        Future<?> future = executorService.submit(runnable);
        return future;
    }

    public Future<?> setInputPrintStreamUtf8(Consumer<PrintStream> writerCallback) {
        return setInputPrintStream(StandardCharsets.UTF_8, true, writerCallback);
    }

    public Future<?> setInputPrintStream(Charset charset, boolean autoFlush, Consumer<PrintStream> writerCallback) {
        return setInputGenerator(out -> writerCallback.accept(new PrintStream(out, autoFlush, charset)));
    }

    public OutputStream getOutputStream() {
        // fdTable.getFd(0).isOpen();
        // System.out.println("Write End - open: " + fdTable.getFd(0).isOpen());
        return ((FdResourceOutputStream)fdTable.getResource(0)).outputStream();
    }

    public InputStream getInputStream() {
        return ((FdResourceInputStream)fdTable.getFd(1).get()).inputStream();
    }

    public InputStream getErrorStream() {
        return ((FdResourceInputStream)fdTable.getFd(2).get()).inputStream();
    }

    public static ProcessBuilder clone(ProcessBuilder original) {
        ProcessBuilder clone = new ProcessBuilder();
        clone.command(original.command());
        clone.environment().putAll(original.environment());
        clone.redirectInput(original.redirectInput());
        clone.redirectOutput(original.redirectOutput());
        clone.redirectError(original.redirectError());
        clone.directory(original.directory());
        return clone;
    }

    public ProcessBuilder configure(ProcessBuilder processBuilder) {
        ProcessBuilder clone = clone(processBuilder);
        configureInput(clone.redirectInput(), fd0, fd0OverridesInherit, clone::redirectInput);
        configureOutput(clone.redirectOutput(), fd1, fd1OverridesInherit, clone::redirectOutput);
        configureOutput(clone.redirectError(), fd2, fd2OverridesInherit, clone::redirectError);
        return clone;
    }

    /** Does not alter the provided process builder. */
    public Process start(ProcessBuilder nativeProcessBuilder) throws IOException {
        ProcessBuilder configuredProcessBuilder = configure(nativeProcessBuilder);
        Process result = configuredProcessBuilder.start();
        return result;
    }

    static Timer timer = null; //new Timer();

    public static ProcessRunner create() throws IOException {
        Path basePath = Files.createTempDirectory("process-exec-");
        System.out.println("Created path at  " + basePath);
        ProcessRunner result = ProcessRunner.create(basePath);
        return result;
    }

    public static ProcessRunner create(Path basePath) throws IOException {
        return create(basePath, true, true, true);
    }

    public static ProcessRunner create(Path basePath, boolean fd0OverridesInherit, boolean fd1OverridesInherit, boolean fd2OverridesInherit) throws IOException {
        Path fd0 = basePath.resolve("fd0");
        Path fd1 = basePath.resolve("fd1");
        Path fd2 = basePath.resolve("fd2");

        PathLifeCycle lifeCycle = PathLifeCycles.deleteAfterExec(PathLifeCycles.namedPipe());
        PathResource rfd0 = new PathResource(fd0, lifeCycle);
        PathResource rfd1 = new PathResource(fd1, lifeCycle);
        PathResource rfd2 = new PathResource(fd2, lifeCycle);

        rfd0.open();
        rfd1.open();
        rfd2.open();

        Runnable[] closer = {null};

        // Use a thread to open the process-facing ends of the pipes and hold them.
        Thread internalPipeEndOpenerThread = new Thread(() -> {
            try {
                // FileDescriptor.in
                // Open READ end of input pipe
                InputStream xfd0 = new FileInputStream(rfd0.getPath().toFile()); // Files.newInputStream(rfd0.getPath());

                if (false) {
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            System.out.println("Hijacking input");
                            PGroup.readerThread(xfd0, "hijack: ").start();
                        }
                    }, 5000);
                }

                OutputStream xfd1 = Files.newOutputStream(rfd1.getPath());
                OutputStream xfd2 = Files.newOutputStream(rfd2.getPath());

                closer[0] = () -> {
                    System.out.println("Closing ends of process-facing pipes.");
                    try {
                        xfd0.close();
                        xfd1.close();
                        xfd2.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                };
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Pipe ends openend - pipe end opener thread terminated.");
        });
        internalPipeEndOpenerThread.start();

        FdTable fdTable = new FdTable<>();
        // Open the client facing ends of the pipes.
        fdTable.setFd(0, FileDescriptions.of(Files.newOutputStream(rfd0.getPath())));

        // Cannot connect to a file input stream if it hasn't been opened for writing
        fdTable.setFd(1, FileDescriptions.of(Files.newInputStream(rfd1.getPath())));
        fdTable.setFd(2, FileDescriptions.of(Files.newInputStream(rfd2.getPath())));

        try {
            internalPipeEndOpenerThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Got internal ends");

        Runnable closeAction = closer[0];

        return new ProcessRunner(basePath, rfd0, rfd1, rfd2, fdTable, fd0OverridesInherit, fd1OverridesInherit, fd2OverridesInherit, closeAction);
    }

    @Override
    public void close() throws Exception {
        // TODO Clean up / harden clean up procedure.
        // inThread.cancel(true);
        executorService.shutdown();
        try {
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            List<Runnable> abandonedTasks = executorService.shutdownNow();
            if (!abandonedTasks.isEmpty()) {
                logger.error("Abandoned " + abandonedTasks.size() + " tasks.");
            }
        }

//        if (inThread != null) {
//            inThread.interrupt();
//            try {
//                inThread.join(5000);
//            } catch (InterruptedException e) {
//                logger.warn("Abandoning data provider thread because it did not shut down in time.");
//            }
//        }

        fd0.close();
        fdTable.close();

        if (errThread != null) { errThread.join(); }
        if (outThread != null) { outThread.join(); }

        fd1.close();
        fd2.close();

        Runnable ca = closeAction;
        if (ca != null) {
            ca.run();
        }
        Files.deleteIfExists(basePath);
    }

    static void readLines(InputStream in, Charset charset, Consumer<String> lineCallback) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, charset))) {
            br.lines().forEach(lineCallback::accept);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
