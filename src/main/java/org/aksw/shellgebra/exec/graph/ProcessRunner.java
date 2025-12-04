package org.aksw.shellgebra.exec.graph;

import java.io.BufferedReader;
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
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.aksw.commons.util.docker.Argv;
import org.aksw.commons.util.docker.ContainerUtils;
import org.aksw.shellgebra.exec.graph.JRedirect.PRedirectFileDescription;
import org.aksw.shellgebra.exec.graph.JRedirect.PRedirectIn;
import org.aksw.shellgebra.exec.graph.JRedirect.PRedirectJava;
import org.aksw.shellgebra.exec.graph.JRedirect.PRedirectOut;
import org.aksw.shellgebra.exec.graph.JRedirect.PRedirectPBF;
import org.aksw.vshell.registry.JvmCommand;
import org.aksw.vshell.registry.JvmCommandRegistry;
import org.aksw.vshell.registry.ProcessBuilderJvm;
import org.aksw.vshell.registry.ProcessOverCompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessRunner
    implements AutoCloseable
{
    private static final Logger logger = LoggerFactory.getLogger(ProcessRunner.class);

    // Bridge to java commands.
    private JvmCommandRegistry jvmCmdRegistry;
    private Map<String, String> environment;

    private Path basePath;

    private PosixPipe pipeIn;
    private PosixPipe pipeOut;
    private PosixPipe pipeErr;

    // Overrides for whether inherit stdin/stdout/stderr from the system (this jvm process) rather than the pipes.

    private boolean inheritInFromSystem = false;
    private boolean inheritOutFromSystem = false;
    private boolean inheritErrFromSystem = false;

    private ExecutorService executorService;

    // Threads seem to be more easy to cancel (interrupt) and join than futures.
    private Thread inFuture = null;
    private Thread outFuture = null;
    private Thread errFuture = null;

    // private CompletableFuture<?> inFuture = null;
    // private CompletableFuture<?> outFuture = null;
    // private CompletableFuture<?> errFuture = null;

    // Should there be a process-builder base class that resolves redirects?
    private ProcessCxt cxt; // FIXME Move some fields into process context?

    public ProcessRunner(
            Path basePath, // Do we still need basePath?
            PosixPipe pipeIn, PosixPipe pipeOut, PosixPipe pipeErr,
            boolean inheritInFromSystem, boolean inheritOutFromSystem, boolean inheritErrFromSystem) {
        super();
        this.basePath = basePath;
        this.executorService = Executors.newCachedThreadPool();
        this.inheritInFromSystem = inheritInFromSystem;
        this.inheritOutFromSystem = inheritOutFromSystem;
        this.inheritErrFromSystem = inheritErrFromSystem;

        this.pipeIn = pipeIn;
        this.pipeOut = pipeOut;
        this.pipeErr = pipeErr;

        this.jvmCmdRegistry = new JvmCommandRegistry();
    }

    public JvmCommandRegistry getJvmCmdRegistry() {
        return jvmCmdRegistry;
    }

    public Map<String, String> environment() {
        return environment;
    }

    public Path inputPipe() {
        return pipeIn.getReadEndProcPath();
    }

    public Path outputPipe() {
        return pipeOut.getWriteEndProcPath();
    }

    public Path errorPipe() {
        return pipeErr.getWriteEndProcPath();
    }

    public InputStream internalIn() {
        return pipeIn.getInputStream();
    }

    public OutputStream internalOut() {
        return pipeOut.getOutputStream();
    }

    public OutputStream internalErr() {
        return pipeErr.getOutputStream();
    }

    public PrintStream internalPrintOut() {
        return pipeOut.printer(StandardCharsets.UTF_8);
    }

    public PrintStream internalPrintErr() {
        return pipeErr.printer(StandardCharsets.UTF_8);
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
        Runnable runnable = () -> {
            try (InputStream in = getInputStream()) {
                reader.accept(in);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        //outFuture = CompletableFuture.runAsync(runnable, executorService);
        outFuture = new Thread(runnable);
        outFuture.start();
        return outFuture;
    }

    public Thread setOutputLineReaderUtf8(Consumer<String> lineCallback) {
        return setOutputLineReader(StandardCharsets.UTF_8, lineCallback);
    }

    public Thread setOutputLineReader(Charset charset, Consumer<String> lineCallback) {
        return setOutputReader(in -> readLines(in, charset, lineCallback));
    }

    public Thread setErrorReader(Consumer<InputStream> reader) {
        Runnable runnable = () -> {
            try (InputStream in = getErrorStream()) {
                reader.accept(in);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        errFuture = new Thread(runnable); // CompletableFuture.runAsync(runnable, executorService);
        errFuture.start();
        return errFuture;
    }

    public Thread setErrorLineReaderUtf8(Consumer<String> lineCallback) {
        return setErrorLineReader(StandardCharsets.UTF_8, lineCallback);
    }

    public Thread setErrorLineReader(Charset charset, Consumer<String> lineCallback) {
        return setErrorReader(in -> readLines(in, charset, lineCallback));
    }

    public Thread setInputGenerator(Consumer<OutputStream> inputSupplier) {
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
        inFuture = new Thread(runnable);// CompletableFuture.runAsync(runnable, executorService);
        inFuture.start();
        return inFuture;
    }

    public Thread setInputPrintStreamUtf8(Consumer<PrintStream> writerCallback) {
        return setInputPrintStream(StandardCharsets.UTF_8, true, writerCallback);
    }

    public Thread setInputPrintStream(Charset charset, boolean autoFlush, Consumer<PrintStream> writerCallback) {
        return setInputGenerator(out -> writerCallback.accept(new PrintStream(out, autoFlush, charset)));
    }

    public OutputStream getOutputStream() {
        return pipeIn.getOutputStream();
    }

    public InputStream getInputStream() {
        return pipeOut.getInputStream();
    }

    public InputStream getErrorStream() {
        return pipeErr.getInputStream();
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
        configureInput(clone.redirectInput(), inputPipe(), inheritInFromSystem, clone::redirectInput);
        configureOutput(clone.redirectOutput(), outputPipe(), inheritOutFromSystem, clone::redirectOutput);
        configureOutput(clone.redirectError(), errorPipe(), inheritErrFromSystem, clone::redirectError);
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
            int exitValue = cmd.run(ProcessRunner.this, a);
            return exitValue;
        });
        return process;
    }

    public Process start(ProcessBuilder2 processBuilder) {
        JRedirect redirectIn = processBuilder.redirectInput();
        redirectIn.accept(new JRedirectVisitor<Object>() {
            @Override
            public Object visit(PRedirectJava redirect) {
                // TODO Auto-generated method stub
                return null;
            }
            @Override
            public Object visit(PRedirectFileDescription redirect) {
                throw new UnsupportedOperationException();
            }
            @Override
            public Object visit(PRedirectIn redirect) {
                throw new UnsupportedOperationException();
            }
            @Override
            public Object visit(PRedirectOut redirect) {
                throw new UnsupportedOperationException();
            }
            @Override
            public Object visit(PRedirectPBF redirect) {
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
        PosixPipe inPipe = PosixPipe.open();
        PosixPipe outPipe = PosixPipe.open();
        PosixPipe errPipe = PosixPipe.open();

        return new ProcessRunner(basePath, inPipe, outPipe, errPipe, fd0OverridesInherit, fd1OverridesInherit, fd2OverridesInherit);
    }

    private void cancelAndGet(CompletableFuture<?> future) throws InterruptedException, ExecutionException {
        if (future != null) {
            future.cancel(true);
            future.get();
        }
    }

    private void cancelAndGet(Thread thread) throws InterruptedException, ExecutionException {
        if (thread != null) {
            thread.interrupt();
            thread.join();
        }
    }

    @Override
    public void close() throws Exception {

        cancelAndGet(inFuture);
        // Close the internal output pipe ends to indicate EOF to the outside readers.
        internalIn().close();

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

        internalOut().close();
        internalErr().close();

        cancelAndGet(outFuture);
        cancelAndGet(errFuture);

        pipeIn.close();
        pipeOut.close();
        pipeErr.close();

        Files.deleteIfExists(basePath);
    }

    static void readLines(InputStream in, Charset charset, Consumer<String> lineCallback) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, charset))) {
            br.lines().forEach(lineCallback::accept);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    // Begin of internal streams - pipe ends facing to the processes.
//
//    private BufferedReader internalInReader;
//    private Charset internalInCharset;
//    private BufferedWriter internalOutWriter;
//    private Charset internalOutCharset;
//    private BufferedWriter internalErrWriter;
//    private Charset internalErrCharset;
//
//    public final BufferedReader internalInReader() {
//        return internalInReader(Charset.defaultCharset());
//    }
//
//    public final BufferedReader internalInReader(Charset charset) {
//        Objects.requireNonNull(charset, "charset");
//        synchronized (this) {
//            if (internalInReader == null) {
//                internalInCharset = charset;
//                internalInReader = new BufferedReader(new InputStreamReader(internalIn(), charset));
//            } else {
//                if (!internalInCharset.equals(charset))
//                    throw new IllegalStateException("BufferedWriter was created with charset: " + internalInCharset);
//            }
//            return internalInReader;
//        }
//    }
//
//    public final BufferedWriter internalOutWriter() {
//        return internalOutWriter(Charset.defaultCharset());
//    }
//
//    public final BufferedWriter internalOutWriter(Charset charset) {
//        Objects.requireNonNull(charset, "charset");
//        synchronized (this) {
//            if (internalOutWriter == null) {
//                internalOutCharset = charset;
//                internalOutWriter = new BufferedWriter(new OutputStreamWriter(internalOut(), charset));
//            } else {
//                if (!internalOutCharset.equals(charset))
//                    throw new IllegalStateException("BufferedReader was created with charset: " + internalOutCharset);
//            }
//            return internalOutWriter;
//        }
//    }
//
//    public final BufferedWriter internalErrWriter() {
//        return internalErrWriter(Charset.defaultCharset());
//    }
//
//    public final BufferedWriter internalErrWriter(Charset charset) {
//        Objects.requireNonNull(charset, "charset");
//        synchronized (this) {
//            if (internalErrWriter == null) {
//                internalErrCharset = charset;
//                internalErrWriter = new BufferedWriter(new OutputStreamWriter(internalOut(), charset));
//            } else {
//                if (!internalErrCharset.equals(charset))
//                    throw new IllegalStateException("BufferedReader was created with charset: " + internalErrCharset);
//            }
//            return internalErrWriter;
//        }
//    }
//
//    // End of internal streams.
//
//
//    // Mechanism taken from Process
//    // XXX Subclass from process? Or make this an abstract base class?
//
//    // Readers and Writers created for this process; so repeated calls return the same object
//    // All updates must be done while synchronized on this Process.
//    private BufferedWriter outputWriter;
//    private Charset outputCharset;
//    private BufferedReader inputReader;
//    private Charset inputCharset;
//    private BufferedReader errorReader;
//    private Charset errorCharset;
//
//    public final BufferedWriter outputWriter() {
//        return outputWriter(Charset.defaultCharset());
//    }
//
//    public final BufferedWriter outputWriter(Charset charset) {
//        Objects.requireNonNull(charset, "charset");
//        synchronized (this) {
//            if (outputWriter == null) {
//                outputCharset = charset;
//                outputWriter = new BufferedWriter(new OutputStreamWriter(getOutputStream(), charset));
//            } else {
//                if (!outputCharset.equals(charset))
//                    throw new IllegalStateException("BufferedWriter was created with charset: " + outputCharset);
//            }
//            return outputWriter;
//        }
//    }
//
//    public final BufferedWriter inputReader() {
//        return outputWriter(Charset.defaultCharset());
//    }
//
//    public final BufferedReader inputReader(Charset charset) {
//        Objects.requireNonNull(charset, "charset");
//        synchronized (this) {
//            if (inputReader == null) {
//                inputCharset = charset;
//                inputReader = new BufferedReader(new InputStreamReader(getInputStream(), charset));
//            } else {
//                if (!inputCharset.equals(charset))
//                    throw new IllegalStateException("BufferedReader was created with charset: " + inputCharset);
//            }
//            return inputReader;
//        }
//    }
//
//    public final BufferedWriter errorReader() {
//        return outputWriter(Charset.defaultCharset());
//    }
//
//    public final BufferedReader errorReader(Charset charset) {
//        Objects.requireNonNull(charset, "charset");
//        synchronized (this) {
//            if (errorReader == null) {
//                errorCharset = charset;
//                errorReader = new BufferedReader(new InputStreamReader(getErrorStream(), charset));
//            } else {
//                if (!errorCharset.equals(charset))
//                    throw new IllegalStateException("BufferedReader was created with charset: " + errorCharset);
//            }
//            return errorReader;
//        }
//    }

    // End of Process
}

