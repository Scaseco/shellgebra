package org.aksw.shellgebra.exec.graph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.aksw.vshell.registry.ProcessBase;
import org.aksw.vshell.registry.ProcessBase.OutboundIo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessIoWrapper
    implements AutoCloseable
{
    public interface ThrowingConsumer<T> { void accept(T item) throws IOException; }


    private static final Logger logger = LoggerFactory.getLogger(ProcessIoWrapper.class);
    private OutboundIo process;

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

    private ProcessIoWrapper(OutboundIo process) {
        super();
        this.process = process;
        this.executorService = Executors.newCachedThreadPool();
    }

    public static ProcessIoWrapper of(OutboundIo process) {
        return new ProcessIoWrapper(process);
    }

    public Thread setOutputReader(Consumer<InputStream> reader) {
        Runnable runnable = () -> {
            try (InputStream in = getInputStream()) {
                reader.accept(in);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
        //outFuture = CompletableFuture.runAsync(runnable, executorService);
        outFuture = new Thread(runnable);
        outFuture.start();
        return outFuture;
    }

    public Thread setErrorReader(Consumer<InputStream> reader) {
        Runnable runnable = () -> {
            InputStream tmp = null;
            try (InputStream in = getErrorStream()) {
                tmp = in;
                reader.accept(in);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
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
                if (inputSupplier != null) {
                    inputSupplier.accept(out);
                }
                out.flush();
                // FIXME Ideally the stream would always be a FileOutputStream.
                // logger.info("Closing input generator file descriptor: " + SysRuntime.getFdPath(((FileOutputStream)out).getFD()));
            } catch (IOException e) {
                // e.printStackTrace();
                throw new UncheckedIOException(e);
            }
        };
        inFuture = new Thread(runnable);// CompletableFuture.runAsync(runnable, executorService);
        inFuture.start();
        return inFuture;
    }

    public OutboundIo getOutboundIo() {
        return process;
    }

    public OutputStream getOutputStream() {
        return process.toIn();
    }

    public InputStream getInputStream() {
        return process.fromOut();
    }

    public InputStream getErrorStream() {
        return process.fromErr();
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

    public Thread setInputWriterUtf8(ThrowingConsumer<BufferedWriter> writerCallback) {
        return setInputWriter(StandardCharsets.UTF_8, writerCallback);
    }

    public Thread setInputWriter(Charset charset, ThrowingConsumer<BufferedWriter> writerCallback) {
        return setInputGenerator(out -> {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, charset))) {
                writerCallback.accept(writer);
                writer.flush();
            } catch (IOException e) {
                String msg = e.getMessage();
                // Ignore broken pipe on the input.
                if (msg != null && msg.contains("Broken pipe")) {
                    return;
                }
                throw new UncheckedIOException(e);
            }
        });
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

    // Wait for writer threads to exit.
    public void waitFor() throws InterruptedException {
        outFuture.join();
        errFuture.join();
        inFuture.join();
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

        cancelAndGet(outFuture);
        cancelAndGet(errFuture);
    }

    public record ExecResult(int exitCode, String out, String err) {}

    public static class Builder {
        //private Process process;
        private OutboundIo io;
        private Process process; // optional

        private Consumer<OutputStream> toIn;
        private Consumer<InputStream> fromOut;
        private Consumer<InputStream> fromErr;

        private Builder(OutboundIo io, Process process) {
            super();
            this.io = io;
            this.process = process;
        }

        public Builder setOutputReader(Consumer<InputStream> reader) {
            this.fromOut = reader;
            return this;
        }

        public Builder setErrorReader(Consumer<InputStream> reader) {
            this.fromErr = reader;
            return this;
        }

        public Builder setInputGenerator(Consumer<OutputStream> inputSupplier) {
            this.toIn = inputSupplier;
            return this;
        }

        public Builder setOutputLineReaderUtf8(Consumer<String> lineCallback) {
            return setOutputLineReader(StandardCharsets.UTF_8, lineCallback);
        }

        public Builder setOutputLineReader(Charset charset, Consumer<String> lineCallback) {
            return setOutputReader(in -> readLines(in, charset, lineCallback));
        }

        public Builder setErrorLineReaderUtf8(Consumer<String> lineCallback) {
            return setErrorLineReader(StandardCharsets.UTF_8, lineCallback);
        }

        public Builder setErrorLineReader(Charset charset, Consumer<String> lineCallback) {
            return setErrorReader(in -> readLines(in, charset, lineCallback));
        }

        public Builder setInputWriterUtf8(ThrowingConsumer<BufferedWriter> writerCallback) {
            return setInputWriter(StandardCharsets.UTF_8, writerCallback);
        }

        public Builder setInputWriter(Charset charset, ThrowingConsumer<BufferedWriter> writerCallback) {
            return setInputGenerator(out -> {
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, charset))) {
                    writerCallback.accept(writer);
                    writer.flush();
                } catch (IOException e) {
                    String msg = e.getMessage();
                    // Ignore broken pipe on the input.
                    if (msg != null && msg.contains("Broken pipe")) {
                        return;
                    }
                    throw new UncheckedIOException(e);
                }
            });
        }

        public ProcessIoWrapper exec() {
            ProcessIoWrapper wrapper = ProcessIoWrapper.of(io);
            wrapper.setInputGenerator(toIn);
            wrapper.setOutputReader(fromOut);
            wrapper.setErrorReader(fromErr);
            return wrapper;
        }

        public ExecResult consume() throws InterruptedException {
            StringBuilder outBuilder = new StringBuilder();
            StringBuilder errBuilder = new StringBuilder();
            Builder builder = this;

            builder.setOutputLineReaderUtf8(str -> {
                // System.out.println("got output line: " + str);
                if (!outBuilder.isEmpty()) {
                    outBuilder.append("\n");
                }
                outBuilder.append(str);
            });
            // wrapper.setErrorLineReaderUtf8(logger::info);
            builder.setErrorLineReaderUtf8(str -> {
                // System.out.println("got error line: " + str);
                if (!errBuilder.isEmpty()) {
                    errBuilder.append("\n");
                }
                errBuilder.append(str);
            });
            ProcessIoWrapper wrapper = builder.exec();
            if (process != null) {
                process.waitFor();
            }
            logger.debug("All processes completed.");

             wrapper.waitFor();

            int exitValue = process == null ? 0 : process.exitValue();

            ExecResult result = new ExecResult(exitValue, outBuilder.toString(), errBuilder.toString());
            return result;
        }
    }

    public static Builder builder(OutboundIo io) {
        return new Builder(io, null);
    }

    public static Builder builder(Process process) {
        OutboundIo io = ProcessBase.getOutboundIo(process);
        return new Builder(io, process);
    }
}
