package org.aksw.shellgebra.exec.graph;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;
import java.lang.ProcessBuilder.Redirect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.aksw.shellgebra.exec.IProcessBuilder;
import org.aksw.shellgebra.exec.SysRuntime;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectFileDescription;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectIn;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectJava;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectOut;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectPBF;
import org.aksw.vshell.registry.FileInputSource;
import org.aksw.vshell.registry.FileOutputTarget;
import org.aksw.vshell.registry.JvmCommandRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessRunnerPosix
    implements ProcessRunner
{
    private static final Logger logger = LoggerFactory.getLogger(ProcessRunner.class);

    // Bridge to java commands.
    private JvmCommandRegistry jvmCmdRegistry;
    private Map<String, String> environment;
    private Path directory; // Default working directory. Will be set if process builders don't specify their own.

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

    public ProcessRunnerPosix(
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

    @Override
    public JvmCommandRegistry getJvmCmdRegistry() {
        return jvmCmdRegistry;
    }

    @Override
    public Map<String, String> environment() {
        return environment;
    }

//    public FileInputSource asSource() {
//        return FileInputSource.of(getReadEndProcPath(), in);
//    }
//
//    public FileOutputTarget asTarget() {
//        return FileOutputTarget.of(getWriteEndProcPath(), out);
//    }

    @Override
    public Path inputPipe() {
        return pipeIn.getReadEndProcPath();
    }

    @Override
    public Path outputPipe() {
        return pipeOut.getWriteEndProcPath();
    }

    @Override
    public Path errorPipe() {
        return pipeErr.getWriteEndProcPath();
    }

    @Override
    public FileInputSource internalIn() {
        return FileInputSource.of(pipeIn.getReadEndProcPath(), pipeIn.getInputStream());
        // return pipeIn.getInputStream();
    }

    @Override
    public FileOutputTarget internalOut() {
        return FileOutputTarget.of(pipeOut.getWriteEndProcPath(), pipeOut.getOutputStream());
        // return pipeOut.getOutputStream();
    }

    @Override
    public FileOutputTarget internalErr() {
        return FileOutputTarget.of(pipeErr.getWriteEndProcPath(), pipeErr.getOutputStream());
        // return pipeErr.getOutputStream();
    }

    @Override
    public PrintStream internalPrintOut() {
        return pipeOut.printer(StandardCharsets.UTF_8);
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
    public Thread setInputGenerator(Consumer<OutputStream> inputSupplier) {
        Runnable runnable = () -> {
            try (OutputStream out = getOutputStream()) {
                inputSupplier.accept(out);
                out.flush();
                logger.info("Closing: " + SysRuntime.getFdPath(((FileOutputStream)out).getFD()));
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        };
        inFuture = new Thread(runnable);// CompletableFuture.runAsync(runnable, executorService);
        inFuture.start();
        return inFuture;
    }

    @Override
    public OutputStream getOutputStream() {
        return pipeIn.getOutputStream();
    }

    @Override
    public InputStream getInputStream() {
        return pipeOut.getInputStream();
    }

    @Override
    public InputStream getErrorStream() {
        return pipeErr.getInputStream();
    }

    @Override
    public Path directory() {
        return directory;
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

    public IProcessBuilder<?> configure(IProcessBuilder<?> processBuilder) {
        IProcessBuilder<?> clone = processBuilder.clone();

        // TODO Properly process the redirects
        clone.redirectInput(new JRedirectJava(Redirect.from(pipeIn.getReadEndProcFile())));
        clone.redirectOutput(new JRedirectJava(Redirect.to(pipeOut.getWriteEndProcFile())));
        clone.redirectError(new JRedirectJava(Redirect.to(pipeIn.getWriteEndProcFile())));
        return clone;
    }

    /** Does not alter the provided process builder. */
//    @Override
//    public Process start(ProcessBuilder nativeProcessBuilder) throws IOException {
//        ProcessBuilder configuredProcessBuilder = configure(nativeProcessBuilder);
//        Process result = configuredProcessBuilder.start();
//        return result;
//    }

    //public Process startDocker(ProcessBuilderDocker processBuilder) {
    //	ProcessBuilderDocker.start(this);
    //}

//    @Override
//    public Process startJvm(ProcessBuilderJvm jvmProcessBuilder) {
//        Argv a = Argv.of(jvmProcessBuilder.command());
//        String command = a.command();
//        JvmCommand cmd = getJvmCmdRegistry().get(command)
//                .orElseThrow(() -> new RuntimeException("Command not found: " + command));
//        Process process = ProcessOverCompletableFuture.of(() -> {
//        	JvmExecCxt execCxt = new JvmExecCxt(this, environment(), directory(), )
//
//            int exitValue = cmd.run(ProcessRunnerPosix.this, a);
//            return exitValue;
//        });
//        return process;
//    }

    public static ProcessRunner create() throws IOException {
        Path basePath = Files.createTempDirectory("process-exec-");
        System.out.println("Created path at  " + basePath);
        ProcessRunner result = ProcessRunnerPosix.create(basePath);
        return result;
    }

    public static ProcessRunner create(Path basePath) throws IOException {
        return create(basePath, true, true, true);
    }

    public static ProcessRunner create(Path basePath, boolean fd0OverridesInherit, boolean fd1OverridesInherit, boolean fd2OverridesInherit) throws IOException {
        PosixPipe inPipe = PosixPipe.open();
        PosixPipe outPipe = PosixPipe.open();
        PosixPipe errPipe = PosixPipe.open();

        return new ProcessRunnerPosix(basePath, inPipe, outPipe, errPipe, fd0OverridesInherit, fd1OverridesInherit, fd2OverridesInherit);
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
        internalIn().inputStream().close();

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

        internalOut().outputStream().close();
        internalErr().outputStream().close();

        cancelAndGet(outFuture);
        cancelAndGet(errFuture);

        pipeIn.close();
        pipeOut.close();
        pipeErr.close();

        Files.deleteIfExists(basePath);
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
}
