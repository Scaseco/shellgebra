package org.aksw.shellgebra.exec.graph;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.aksw.shellgebra.exec.IProcessBuilder;
import org.aksw.shellgebra.exec.SysRuntime;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectJava;
import org.aksw.shellgebra.io.pipe.PosixPipe;
import org.aksw.vshell.registry.FileInput;
import org.aksw.vshell.registry.FileOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//public class ProcessRunnerPosixBase
//    implements ProcessRunner
//{
//    private static final Logger logger = LoggerFactory.getLogger(ProcessRunnerPosix.class);
//
//    // Bridge to java commands.
//    // private JvmCommandRegistry jvmCmdRegistry;
//    private Map<String, String> environment;
//    private Path directory; // Default working directory. Will be set if process builders don't specify their own.
//
//    private Path basePath;
//
//    private PosixPipe pipeIn;
//    private PosixPipe pipeOut;
//    private PosixPipe pipeErr;
//
//    // boolean inheritInFromSystem, boolean inheritOutFromSystem, boolean inheritErrFromSystem
//    public ProcessRunnerPosixBase(
//            Path basePath, // Do we still need basePath?
//            PosixPipe pipeIn, PosixPipe pipeOut, PosixPipe pipeErr) {
//        super();
//        this.basePath = basePath;
//
//        this.pipeIn = pipeIn;
//        this.pipeOut = pipeOut;
//        this.pipeErr = pipeErr;
//    }
//
//    @Override
//    public Map<String, String> environment() {
//        return environment;
//    }
//
//    @Override
//    public Path inputPipe() {
//        return pipeIn.getReadEndProcPath();
//    }
//
//    @Override
//    public Path outputPipe() {
//        return pipeOut.getWriteEndProcPath();
//    }
//
//    @Override
//    public Path errorPipe() {
//        return pipeErr.getWriteEndProcPath();
//    }
//
//    @Override
//    public FileInput internalIn() {
//        return FileInput.of(pipeIn.getReadEndProcPath(), pipeIn.getInputStream());
//    }
//
//    @Override
//    public FileOutput internalOut() {
//        return FileOutput.of(pipeOut.getWriteEndProcPath(), pipeOut.getOutputStream());
//    }
//
//    @Override
//    public FileOutput internalErr() {
//        return FileOutput.of(pipeErr.getWriteEndProcPath(), pipeErr.getOutputStream());
//    }
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
//    public Path directory() {
//        return directory;
//    }
//
//    public static ProcessRunner create() throws IOException {
//        Path basePath = Files.createTempDirectory("process-exec-");
//        logger.debug("Created temporary directory for named and anonymous pipes at  " + basePath);
//        ProcessRunner result = ProcessRunnerPosix.create(basePath);
//        return result;
//    }
//
//    public static ProcessRunner create(Path basePath) throws IOException {
//        return create(basePath, true, true, true);
//    }
//
//    public static ProcessRunner create(Path basePath, boolean fd0OverridesInherit, boolean fd1OverridesInherit, boolean fd2OverridesInherit) throws IOException {
//        PosixPipe inPipe = PosixPipe.open();
//        PosixPipe outPipe = PosixPipe.open();
//        PosixPipe errPipe = PosixPipe.open();
//
//        return new ProcessRunnerPosix(basePath, inPipe, outPipe, errPipe, fd0OverridesInherit, fd1OverridesInherit, fd2OverridesInherit);
//    }
//
//    private void cancelAndGet(CompletableFuture<?> future) throws InterruptedException, ExecutionException {
//        if (future != null) {
//            future.cancel(true);
//            future.get();
//        }
//    }
//
//    private void cancelAndGet(Thread thread) throws InterruptedException, ExecutionException {
//        if (thread != null) {
//            thread.interrupt();
//            thread.join();
//        }
//    }
//
//    @Override
//    public void shutdown() throws IOException {
//        try {
//            getOutputStream().close();
//            // pipeIn.getOutputStream().close();
//            //internalIn().close();
//        } finally {
//            try {
//    //            getInputStream().close();
//                internalOut().close();
//            } finally {
//    //            getErrorStream().close();
//                internalErr().close();
//            }
//        }
//    }
//
//    @Override
//    public void close() throws Exception {
//        cancelAndGet(inFuture);
//        // Close the internal output pipe ends to indicate EOF to the outside readers.
//        internalIn().inputStream().close();
//
//        // TODO Clean up / harden clean up procedure.
//        // inThread.cancel(true);
//        executorService.shutdown();
//        try {
//            executorService.awaitTermination(5, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            List<Runnable> abandonedTasks = executorService.shutdownNow();
//            if (!abandonedTasks.isEmpty()) {
//                logger.error("Abandoned " + abandonedTasks.size() + " tasks.");
//            }
//        }
//
//        internalOut().outputStream().close();
//        internalErr().outputStream().close();
//
//        cancelAndGet(outFuture);
//        cancelAndGet(errFuture);
//
//        pipeIn.close();
//        pipeOut.close();
//        pipeErr.close();
//
//        Files.deleteIfExists(basePath);
//    }
//
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
//
//    @Override
//    public Thread setOutputReader(Consumer<InputStream> reader) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public Thread setErrorReader(Consumer<InputStream> reader) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public Thread setInputGenerator(Consumer<OutputStream> inputSupplier) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public OutputStream getOutputStream() {
//        return pipeIn.getOutputStream();
//    }
//
//    @Override
//    public InputStream getInputStream() {
//        return pipeOut.getInputStream();
//    }
//
//    @Override
//    public InputStream getErrorStream() {
//        return pipeErr.getInputStream();
//    }
//}
