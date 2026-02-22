package org.aksw.shellgebra.exec.graph;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.aksw.shellgebra.exec.SysRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessIoWrapper
    implements AutoCloseable
{
    private static final Logger logger = LoggerFactory.getLogger(ProcessIoWrapper.class);

    private Process process;

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

    private ProcessIoWrapper(Process process) {
        super();
        this.process = process;
        this.executorService = Executors.newCachedThreadPool();
    }

    public static ProcessIoWrapper of(Process process) {
        return new ProcessIoWrapper(process);
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

    public Thread setInputGenerator(Consumer<OutputStream> inputSupplier) {
        Runnable runnable = () -> {
             try (OutputStream out = getOutputStream()) {
//            try {
//                OutputStream out = getOutputStream();
                inputSupplier.accept(out);
                out.flush();
                // FIXME Ideally the stream would always be a FileOutputStream.
                // logger.info("Closing input generator file descriptor: " + SysRuntime.getFdPath(((FileOutputStream)out).getFD()));
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        };
        inFuture = new Thread(runnable);// CompletableFuture.runAsync(runnable, executorService);
        inFuture.start();
        return inFuture;
    }

    public OutputStream getOutputStream() {
        return process.getOutputStream();
    }

    public InputStream getInputStream() {
        return process.getInputStream();
    }

    public InputStream getErrorStream() {
        return process.getInputStream();
    }

    public Thread setOutputLineReaderUtf8(Consumer<String> lineCallback) {
        return setOutputLineReader(StandardCharsets.UTF_8, lineCallback);
    }

    public Thread setOutputLineReader(Charset charset, Consumer<String> lineCallback) {
        return setOutputReader(in -> readLines(in, charset, lineCallback));
    }

    public Thread setErrorLineReaderUtf8(Consumer<String> lineCallback) {
        return setErrorLineReader(StandardCharsets.UTF_8, lineCallback);
    }

    public Thread setErrorLineReader(Charset charset, Consumer<String> lineCallback) {
        return setErrorReader(in -> readLines(in, charset, lineCallback));
    }

    public Thread setInputPrintStreamUtf8(Consumer<PrintStream> writerCallback) {
        return setInputPrintStream(StandardCharsets.UTF_8, true, writerCallback);
    }

    public Thread setInputPrintStream(Charset charset, boolean autoFlush, Consumer<PrintStream> writerCallback) {
        return setInputGenerator(out -> writerCallback.accept(new PrintStream(out, autoFlush, charset)));
    }

    static void readLines(InputStream in, Charset charset, Consumer<String> lineCallback) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, charset))) {
            br.lines().forEach(lineCallback::accept);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    public static ProcessIoWrapper create() throws IOException {
//        Path basePath = Files.createTempDirectory("process-exec-");
//        logger.debug("Created temporary directory for named and anonymous pipes at  " + basePath);
//        ProcessIoWrapper result = ProcessIoWrapper.create(basePath);
//        return result;
//    }

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

    public void shutdown() throws IOException {
        try {
            getOutputStream().close();
            // pipeIn.getOutputStream().close();
            //internalIn().close();
        } finally {
            try {
//                getInputStream().close();
                getInputStream().close();
            } finally {
//                getErrorStream().close();
                getErrorStream().close();
            }
        }
    }

    public void close() throws Exception {
    }

    public void closeOld() throws Exception {
        cancelAndGet(inFuture);
        // Close the internal output pipe ends to indicate EOF to the outside readers.
        // internalIn().inputStream().close();
        getInputStream().close();

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

        getOutputStream().close();
        getErrorStream().close();
//        internalOut().outputStream().close();
//        internalErr().outputStream().close();

        cancelAndGet(outFuture);
        cancelAndGet(errFuture);

//        pipeIn.close();
//        pipeOut.close();
//        pipeErr.close();

        // Files.deleteIfExists(basePath);
    }

//    public static Builder newBuilder() {
//        return new Builder();
//    }
//
//    public static class Builder {
//        private Consumer<InputStream> inputAction;
//        private Consumer<OutputStream> outputAction;
//        private Consumer<OutputStream> errorAction;
//    }

//    public static ProcessBuilder clone(ProcessBuilder original) {
//        ProcessBuilder clone = new ProcessBuilder();
//        clone.command(original.command());
//        clone.environment().putAll(original.environment());
//        clone.redirectInput(original.redirectInput());
//        clone.redirectOutput(original.redirectOutput());
//        clone.redirectError(original.redirectError());
//        clone.directory(original.directory());
//        return clone;
//    }
//
//    public IProcessBuilder<?> configure(IProcessBuilder<?> processBuilder) {
//        IProcessBuilder<?> clone = processBuilder.clone();
//
//        // TODO Properly process the redirects
//        clone.redirectInput(new JRedirectJava(Redirect.from(pipeIn.getReadEndProcFile())));
//        clone.redirectOutput(new JRedirectJava(Redirect.to(pipeOut.getWriteEndProcFile())));
//        clone.redirectError(new JRedirectJava(Redirect.to(pipeIn.getWriteEndProcFile())));
//        return clone;
//    }
}
