package org.aksw.shellgebra.exec.graph;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.aksw.shellgebra.exec.IProcessBuilder;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectJava;
import org.aksw.shellgebra.io.pipe.DynamicPipe;
import org.aksw.shellgebra.io.pipe.PosixPipe;
import org.aksw.vshell.registry.DynamicInputShared;
import org.aksw.vshell.registry.DynamicOutputShared;
import org.aksw.vshell.registry.ProcessBase.ToInternalIo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessRunnerPosix
    implements ProcessRunner
{
    private static final Logger logger = LoggerFactory.getLogger(ProcessRunnerPosix.class);

    // Bridge to java commands.
    // private JvmCommandRegistry jvmCmdRegistry;
    private Map<String, String> environment;
    private Path directory; // Default working directory. Will be set if process builders don't specify their own.

    private Path basePath;

    private DynamicPipe pipeIn;
    private DynamicPipe pipeOut;
    private DynamicPipe pipeErr;

    private ToInternalIo internalIo;

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
    // private ProcessCxt cxt; // FIXME Move some fields into process context?

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

        this.internalIo = new ToInternalIo(
            DynamicInputShared.of(pipeIn.input()),
            DynamicOutputShared.of(pipeOut.output()),
            DynamicOutputShared.of(pipeErr.output()));

//        // Make sure to init the internal-facing streams.
//        pipeIn.getInputStream();
//        pipeOut.getOutputStream();
//        pipeErr.getOutputStream();

        // this.jvmCmdRegistry = new JvmCommandRegistry();
    }

    @Override
    public void releaseInternalIo() {
        internalIo.close();
//        try {
//            internalIo.fromIn().close();
//            internalIo.toOut().close();
//            internalIo.toErr().close();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

//    @Override
//    public JvmCommandRegistry getJvmCmdRegistry() {
//        return jvmCmdRegistry;
//    }

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

//    @Override
//    public DynamicInput inputPipe() {
//        return pipeIn.input();
//        // return pipeIn.input().getFile();
//        // return pipeIn.getReadEndProcPath();
//    }
//
//    @Override
//    public DynamicOutput outputPipe() {
//        return pipeOut.output();
//        // return pipeOut.output().getFile();
//        // return pipeOut.getWriteEndProcPath();
//    }
//
//    @Override
//    public DynamicOutput errorPipe() {
//        return pipeErr.output();
//        // return pipeErr.output().getFile();
//        // return pipeErr.getWriteEndProcPath();
//    }

    @Override
    public DynamicInputShared internalIn() {
        return internalIo.fromIn();
        // return pipeIn.input();
        // return FileInput.of(pipeIn.getReadEndProcPath(), pipeIn.getInputStream());
    }

    @Override
    public DynamicOutputShared internalOut() {
        return internalIo.toOut();
        // return pipeOut.output();
        // return FileOutput.of(pipeOut.getWriteEndProcPath(), pipeOut.getOutputStream());
    }

    @Override
    public DynamicOutputShared internalErr() {
        return internalIo.toErr();
        // return pipeErr.output();
        // return FileOutput.of(pipeErr.getWriteEndProcPath(), pipeErr.getOutputStream());
    }
//
//    @Override
//    public PrintStream internalPrintOut() {
//        return pipeOut.printer(StandardCharsets.UTF_8);
//    }
//
//    @Override
//    public PrintStream internalPrintErr() {
//        return pipeErr.printer(StandardCharsets.UTF_8);
//    }
//
//    @Override
//    public Thread setOutputReader(Consumer<InputStream> reader) {
//        Runnable runnable = () -> {
//            try (InputStream in = getInputStream()) {
//                reader.accept(in);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        };
//        //outFuture = CompletableFuture.runAsync(runnable, executorService);
//        outFuture = new Thread(runnable);
//        outFuture.start();
//        return outFuture;
//    }
//
//    @Override
//    public Thread setErrorReader(Consumer<InputStream> reader) {
//        Runnable runnable = () -> {
//            try (InputStream in = getErrorStream()) {
//                reader.accept(in);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        };
//        errFuture = new Thread(runnable); // CompletableFuture.runAsync(runnable, executorService);
//        errFuture.start();
//        return errFuture;
//    }
//
//    @Override
//    public Thread setInputGenerator(Consumer<OutputStream> inputSupplier) {
//        Runnable runnable = () -> {
//             try (OutputStream out = getOutputStream()) {
////            try {
////                OutputStream out = getOutputStream();
//                inputSupplier.accept(out);
//                out.flush();
//                logger.info("Closing input generator file descriptor: " + SysRuntime.getFdPath(((FileOutputStream)out).getFD()));
//            } catch (IOException e) {
//                e.printStackTrace();
//                throw new RuntimeException(e);
//            }
//        };
//        inFuture = new Thread(runnable);// CompletableFuture.runAsync(runnable, executorService);
//        inFuture.start();
//        return inFuture;
//    }

    @Override
    public OutputStream getOutputStream() {
        return pipeIn.outputStream();
    }

    @Override
    public InputStream getInputStream() {
        return pipeOut.inputStream();
    }

    @Override
    public InputStream getErrorStream() {
        return pipeErr.inputStream();
    }

    @Override
    public Path directory() {
        return directory;
    }

    public static ProcessRunner create() throws IOException {
        Path basePath = Files.createTempDirectory("process-exec-");
        logger.debug("Created temporary directory for named and anonymous pipes at  " + basePath);
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
    public void shutdown() throws IOException {
        try {
            getOutputStream().close();
            // pipeIn.getOutputStream().close();
            //internalIn().close();
        } finally {
            try {
//                getInputStream().close();
                internalOut().close();
            } finally {
//                getErrorStream().close();
                internalErr().close();
            }
        }
    }

    @Override
    public void close() throws Exception {
//        cancelAndGet(inFuture);
//        // Close the internal output pipe ends to indicate EOF to the outside readers.
//        internalIn().inputStream().close();

        // TODO Clean up / harden clean up procedure.
        // inThread.cancel(true);
        releaseInternalIo();
        executorService.shutdown();
        try {
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            List<Runnable> abandonedTasks = executorService.shutdownNow();
            if (!abandonedTasks.isEmpty()) {
                logger.error("Abandoned " + abandonedTasks.size() + " tasks.");
            }
        }

//        internalOut().outputStream().close();
//        internalErr().outputStream().close();

        cancelAndGet(outFuture);
        cancelAndGet(errFuture);

        Files.deleteIfExists(basePath);
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

    public IProcessBuilder<?> configure(IProcessBuilder<?> processBuilder) {
        IProcessBuilder<?> clone = processBuilder.clone();

        // TODO Properly process the redirects
        clone.redirectInput(new JRedirectJava(Redirect.from(pipeIn.getReadEndProcFile())));
        clone.redirectOutput(new JRedirectJava(Redirect.to(pipeOut.getWriteEndProcFile())));
        clone.redirectError(new JRedirectJava(Redirect.to(pipeIn.getWriteEndProcFile())));
        return clone;
    }
}
