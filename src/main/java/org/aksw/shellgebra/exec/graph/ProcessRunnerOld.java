package org.aksw.shellgebra.exec.graph;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.aksw.commons.util.docker.Argv;
import org.aksw.commons.util.docker.ContainerUtils;
import org.aksw.shellgebra.exec.PathLifeCycle;
import org.aksw.shellgebra.exec.PathLifeCycles;
import org.aksw.shellgebra.exec.graph.FdResource.FdResourceInputStream;
import org.aksw.shellgebra.exec.graph.FdResource.FdResourceOutputStream;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectFileDescription;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectIn;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectJava;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectOut;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectPBF;
import org.aksw.vshell.registry.JvmCommand;
import org.aksw.vshell.registry.JvmCommandRegistry;
import org.aksw.vshell.registry.ProcessBuilderJvm;
import org.aksw.vshell.registry.ProcessOverCompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process runner provides an environment for running a set of processes.
 * All processes can share the same input, output and error streams.
 *
 * The process runner manages a set of  named pipes.
 * Processes launched with this runner are configured with the appropriate pipe ends.
 */
public class ProcessRunnerOld
    implements AutoCloseable
{
    private static final Logger logger = LoggerFactory.getLogger(ProcessRunner.class);

    // Bridge to java commands.
    private JvmCommandRegistry jvmCmdRegistry;
    private Map<String, String> environment;

    private Path basePath;

    private PathResource fd0;
    private PathResource fd1;
    private PathResource fd2;

    private boolean fd0OverridesInherit = false;
    private boolean fd1OverridesInherit = false;
    private boolean fd2OverridesInherit = false;

    // Process-facing streams.
    private List<FileDescription<FdResource>> internalPipeEnds;

    // private Thread dummyThread;
    private Runnable closeAction;


    private FdTable fdTable;

    // Should there be a process-builder base class that resolves redirects?
    private ProcessCxt cxt; // FIXME Move some fields into process context?

    private ExecutorService executorService;

    // private Thread inThread;
    private CompletableFuture<?> inThread;
    private Thread outThread;
    private Thread errThread;

//    private OutputStream in;
//    private InputStream out;
//    private InputStream err;

    public ProcessRunnerOld(Path basePath, PathResource fd0, PathResource fd1, PathResource fd2,
            FdTable fdTable, // Table of outside-facing pipe ends.
            boolean fd0OverridesInherit, boolean fd1OverridesInherit, boolean fd2OverridesInherit,
            // Runnable closeAction,
            List<FileDescription<FdResource>> internalPipeEnds) {
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

        this.internalPipeEnds = internalPipeEnds;
        // this.closeAction = closeAction;

        this.jvmCmdRegistry = new JvmCommandRegistry();
    }

    public JvmCommandRegistry getJvmCmdRegistry() {
        return jvmCmdRegistry;
    }

    public Map<String, String> environment() {
        return environment;
    }


    public Path inputPipe() {
        // return fd0.getPath();
        try {
            Path result = ContainerUtils.getFdPath(((FileInputStream)internalIn()).getFD());
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Path outputPipe() {
//        return fd1.getPath();
        try {
            Path result = ContainerUtils.getFdPath(((FileOutputStream)internalOut()).getFD());
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Path errorPipe() {
//        return fd2.getPath();
        try {
            Path result = ContainerUtils.getFdPath(((FileOutputStream)internalErr()).getFD());
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream internalIn() {
        return internalPipeEnds.get(0).get().asInputStream();
    }

    public OutputStream internalOut() {
        return internalPipeEnds.get(1).get().asOutputStream();
    }

    public OutputStream internalErr() {
        return internalPipeEnds.get(2).get().asOutputStream();
    }

    public PrintStream internalPrintOut() {
        return new PrintStream(internalPipeEnds.get(1).get().asOutputStream());
    }

    public PrintStream internalPrintErr() {
        return new PrintStream(internalPipeEnds.get(2).get().asOutputStream());
    }

//    public JvmExecCxt getJvmContext() {
//        public JvmExecCxt(
//                JvmContext context,
//                // List<String> command,
//                Map<String, String> environment,
//                Path directory,
//                InputStream inputStream, PrintStream outputStream, PrintStream errorStream) {
//    }

    public FdTable getFdTable() {
        return fdTable;
    }

    private void configureInput(Redirect redirect, Path fd, boolean fdOverridesInherit, Consumer<Redirect> redirectConsumer) {
        Type type = redirect.type();
        switch (type) {
        case PIPE:
            redirectConsumer.accept(Redirect.from(fd.toFile()));
            break;
        case INHERIT:
            if (fdOverridesInherit) {
                redirectConsumer.accept(Redirect.from(fd.toFile()));
            }
            break;
        default:
            // nothing to do.
        }
    }

    private void configureOutput(Redirect redirect, Path fd, boolean fdOverridesInherit, Consumer<Redirect> redirectConsumer) {
        Type type = redirect.type();
        switch (type) {
        case PIPE:
            redirectConsumer.accept(Redirect.to(fd.toFile()));
            break;
        case INHERIT:
            if (fdOverridesInherit) {
                redirectConsumer.accept(Redirect.to(fd.toFile()));
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
                logger.info("Closing: " + ContainerUtils.getFdPath(((FileOutputStream)out).getFD()));
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
        OutputStream result = ((FdResourceOutputStream)fdTable.getResource(0)).outputStream();
        return result;
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
        configureInput(clone.redirectInput(), inputPipe(), fd0OverridesInherit, clone::redirectInput);
        configureOutput(clone.redirectOutput(), outputPipe(), fd1OverridesInherit, clone::redirectOutput);
        configureOutput(clone.redirectError(), errorPipe(), fd2OverridesInherit, clone::redirectError);
        return clone;
    }

    /** Does not alter the provided process builder. */
    public Process start(ProcessBuilder nativeProcessBuilder) throws IOException {
        ProcessBuilder configuredProcessBuilder = configure(nativeProcessBuilder);
        Process result = configuredProcessBuilder.start();
        return result;
    }

//    public Process startDocker(ProcessBuilderDocker processBuilder) {
//    	ProcessBuilderDocker.start(this);
//    }

    public Process startJvm(ProcessBuilderJvm jvmProcessBuilder) {
        Argv a = Argv.of(jvmProcessBuilder.command());
        String command = a.command();
        JvmCommand cmd = getJvmCmdRegistry().get(command)
                .orElseThrow(() -> new RuntimeException("Command not found: " + command));
        Process process = ProcessOverCompletableFuture.of(() -> {
            int exitValue = cmd.run(ProcessRunnerOld.this, a);
            return exitValue;
        });
        return process;
    }

    public Process start(ProcessBuilder2 processBuilder) {
        JRedirect redirectIn = processBuilder.redirectInput();
        redirectIn.accept(new JRedirectVisitor<Object>() {
            @Override
            public Object visit(JRedirectJava redirect) {
                // TODO Auto-generated method stub
                return null;
            }
            @Override
            public Object visit(JRedirectFileDescription redirect) {
                throw new UnsupportedOperationException();
            }
            @Override
            public Object visit(JRedirectIn redirect) {
                throw new UnsupportedOperationException();
            }
            @Override
            public Object visit(JRedirectOut redirect) {
                throw new UnsupportedOperationException();
            }
            @Override
            public Object visit(JRedirectPBF redirect) {
                PBF pbf = redirect.pbf();

                return null;
            }
        });

        processBuilder.redirectOutput();
        processBuilder.redirectError();
        // processBuilder
        return null;
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
        Path pipe0 = basePath.resolve("pipe0");
        Path pipe1 = basePath.resolve("pipe1");
        Path pipe2 = basePath.resolve("pipe2");

        PathLifeCycle lifeCycle = PathLifeCycles.deleteAfterExec(PathLifeCycles.namedPipe());
        PathResource rpipe0 = new PathResource(pipe0, lifeCycle);
        PathResource rpipe1 = new PathResource(pipe1, lifeCycle);
        PathResource rpipe2 = new PathResource(pipe2, lifeCycle);

        rpipe0.open();
        rpipe1.open();
        rpipe2.open();

        Runnable[] closer = {null};

//         int fdVal = FileDescriptorCast.using(in.getFD()).as(Integer.class);


        List<FileDescription<FdResource>> internalPipeEnds = new ArrayList<>(3);

        // Use a thread to open the process-facing ends of the pipes and hold them.
        Thread internalPipeEndOpenerThread = new Thread(() -> {
            try {
                // FileDescriptor.in
                // Open READ end of input pipe
                FileInputStream xfd0 = new FileInputStream(rpipe0.getPath().toFile()); // Files.newInputStream(rfd0.getPath());

                if (false) {
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            System.out.println("Hijacking input");
                            PGroup.readerThread(xfd0, "hijack: ").start();
                        }
                    }, 5000);
                }

                FileOutputStream xfd1 = new FileOutputStream(rpipe1.getPath().toFile()); // Files.newOutputStream(rpipe1.getPath());
                FileOutputStream xfd2 = new FileOutputStream(rpipe2.getPath().toFile()); // Files.newOutputStream(rpipe2.getPath());

                logger.info("read end of pipe 0: " + ContainerUtils.getFdPath(xfd0.getFD()));
                logger.info("write end of pipe 1: " + ContainerUtils.getFdPath(xfd1.getFD()));
                logger.info("write end of pipe 2: " + ContainerUtils.getFdPath(xfd2.getFD()));

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

                internalPipeEnds.add(FileDescriptions.of(xfd0));
                internalPipeEnds.add(FileDescriptions.of(xfd1));
                internalPipeEnds.add(FileDescriptions.of(xfd2));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Pipe ends openend - pipe end opener thread terminated.");
        });
        internalPipeEndOpenerThread.start();

        // Open the client facing ends of the pipes.

        FdTable<FdResource> fdTable = new FdTable<>();
        FileOutputStream yfd0 = new FileOutputStream(rpipe0.getPath().toFile());
        fdTable.setFd(0, FileDescriptions.of(yfd0));

        // Cannot connect to a file input stream if it hasn't been opened for writing
        FileInputStream yfd1 = new FileInputStream(rpipe1.getPath().toFile());
        fdTable.setFd(1, FileDescriptions.of(yfd1));

        FileInputStream yfd2 = new FileInputStream(rpipe2.getPath().toFile());
        fdTable.setFd(2, FileDescriptions.of(yfd2));

        logger.info("write end of pipe 0: " + ContainerUtils.getFdPath(yfd0.getFD()));
        logger.info("read end of pipe 1: " + ContainerUtils.getFdPath(yfd1.getFD()));
        logger.info("read end of pipe 2: " + ContainerUtils.getFdPath(yfd2.getFD()));

//        fdTable.setFd(0, FileDescriptions.of(Files.newOutputStream(rpipe0.getPath())));
//
//        // Cannot connect to a file input stream if it hasn't been opened for writing
//        fdTable.setFd(1, FileDescriptions.of(Files.newInputStream(rpipe1.getPath())));
//        fdTable.setFd(2, FileDescriptions.of(Files.newInputStream(rpipe2.getPath())));

        try {
            internalPipeEndOpenerThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Got internal ends");

        Runnable closeAction = closer[0];

        return new ProcessRunnerOld(basePath, rpipe0, rpipe1, rpipe2, fdTable, fd0OverridesInherit, fd1OverridesInherit, fd2OverridesInherit, internalPipeEnds);
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

        // Close the internal output pipe ends to indicate EOF to the outside readers.
        internalOut().close();
        internalErr().close();

        if (errThread != null) { errThread.join(); }
        if (outThread != null) { outThread.join(); }
        fdTable.close();

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
