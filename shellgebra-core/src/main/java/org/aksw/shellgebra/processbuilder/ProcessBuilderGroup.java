package org.aksw.shellgebra.processbuilder;

import java.io.Closeable;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.aksw.shellgebra.exec.graph.JRedirect;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectJava;
import org.aksw.shellgebra.io.pipe.NamedPipe;
import org.aksw.shellgebra.io.pipe.PosixPipe;
import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.aksw.vshell.registry.ProcessOverCompletableFuture;

public class ProcessBuilderGroup
    extends ProcessBuilderCompound<ProcessBuilderGroup>
{
    public static ProcessBuilderGroup of(IProcessBuilderCore<?> ... processBuilders) {
        return new ProcessBuilderGroup().processBuilders(processBuilders);
    }

    public static ProcessBuilderGroup of(List<? extends IProcessBuilderCore<?>> processBuilders) {
        return new ProcessBuilderGroup().processBuilders(processBuilders);
    }

    public ProcessBuilderGroup() {
        super();
    }

    @Override
    protected ProcessBuilderGroup cloneActual() {
        return new ProcessBuilderGroup();
    }

    @Override
    public Process start(ProcessRunner executor) throws IOException {
        List<? extends IProcessBuilderCore<?>> pbs = copyProcessBuilders();
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> run(executor, pbs));
        return new ProcessOverCompletableFuture(future);
    }


    private PathAndProcess processInput(Path inputPipePath, JRedirect redirect, boolean requiresAnonPipe) throws IOException {
        // Extract effective raw input path
        Path rawInputPath = null;
        if (redirect instanceof JRedirectJava x) {
            Redirect r = x.redirect();
            switch (r.type()) {
            case INHERIT:
                rawInputPath = inputPipePath;
                break;
            case READ:
                rawInputPath = r.file().toPath();
                break;
            default:
                throw new RuntimeException("Unsupported or not implemented yet.");
            }
        }

        // If the raw input path is not docker mountable then we start
        // a helper process that pumps the bytes from the raw input path to a mountable named pipe.
        boolean isProbablyNamedPipe = isProbablyNamedPipe(rawInputPath);
        Path resultPath = null;
        Process pumpProcess = null;
        if (isProbablyNamedPipe && requiresAnonPipe) {
            PosixPipe anonPipe = PosixPipe.open();
            pumpProcess = ProcessBuilderDocker.catProcess(rawInputPath, anonPipe.getWriteEndProcPath());
            resultPath = anonPipe.getReadEndProcPath();
        } else {
            resultPath = rawInputPath;
        }
        return new PathAndProcess(resultPath, pumpProcess);
    }

    /**
     *
     * @param outputPipePath
     * @param redirect
     * @param requiresAnonPipe
     * @return
     * @throws IOException
     */
    private PathAndProcess processOutput(Path outputPipePath, JRedirect redirect, boolean requiresAnonPipe) throws IOException {
        // Extract effective raw input path
        Path rawPath = null;
        if (redirect instanceof JRedirectJava x) {
            Redirect r = x.redirect();
            switch (r.type()) {
            case INHERIT:
                rawPath = outputPipePath;
                break;
            case WRITE:
                rawPath = r.file().toPath();
                break;
            default:
                throw new RuntimeException("Unsupported or not implemented yet.");
            }
        }

        // If the raw input path is not docker mountable then we start
        // a helper process that pumps the bytes from the raw input path to a mountable named pipe.
        boolean isProbablyNamedPipe = isProbablyNamedPipe(rawPath);
        Path resultPath = null;
        Process pumpProcess = null;
        Closeable closeAction = null;
        if (isProbablyNamedPipe && requiresAnonPipe) {
            PosixPipe anonPipe = PosixPipe.open();
            pumpProcess = ProcessBuilderDocker.catProcess(anonPipe.getReadEndProcPath(), rawPath);
            resultPath = anonPipe.getWriteEndProcPath();
            closeAction = anonPipe; // () -> anonPipe.close(); // TODO Perhaps only close the write end?
        } else {
            resultPath = rawPath;
        }
        return new PathAndProcess(resultPath, pumpProcess, closeAction);
    }

    private static boolean isProbablyNamedPipe(Path p) throws IOException {
        if (!Files.exists(p)) return false;
        boolean result = NamedPipe.isNamedPipe(p, true);
        return result;
        // return !PosixPipe.isAnonymousProcPipe(p);
    }

    private int run(ProcessRunner runner, Collection<? extends IProcessBuilderCore<?>> processBuilders) {
        try {
            return runCore(runner, processBuilders);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int runCore(ProcessRunner runner, Collection<? extends IProcessBuilderCore<?>> processBuilders) throws IOException {
        Set<Process> runningProcesses = Collections.synchronizedSet(new LinkedHashSet<>(processBuilders.size()));
        int result = 0;

        boolean requiresAnonPipeIn = processBuilders().stream().filter(member -> member.accessesStdIn()).count() > 1;
        boolean requiresAnonPipeOutAndErr = processBuilders().size() > 1;

        // If there are multiple group members then any redirect based on a named pipe redirect must
        // be replaced with a bridge that uses an anon pipe.

        // TODO/FIXME It must be ensured that redirects of type INHERIT do not fall back to named pipes (only anon pipes allowed).
        PathAndProcess inProcess = processInput(runner.inputPipe(), redirectInput(), requiresAnonPipeIn);
        PathAndProcess outProcess = processOutput(runner.outputPipe(), redirectOutput(), requiresAnonPipeOutAndErr);
        PathAndProcess errProcess = processOutput(runner.errorPipe(), redirectError(), requiresAnonPipeOutAndErr);

        for (IProcessBuilderCore<?> pb : processBuilders) {

//            pb.redirectInput(redirectInput());
//            pb.redirectOutput(redirectOutput());
//            pb.redirectError(redirectError());

            pb.redirectInput(new JRedirectJava(Redirect.from(inProcess.path().toFile())));
            pb.redirectOutput(new JRedirectJava(Redirect.to(outProcess.path().toFile())));
            pb.redirectError(new JRedirectJava(Redirect.to(errProcess.path().toFile())));

            Process process;
            try {
                System.out.println("Process started from: " + pb);
                process = pb.start(runner);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            runningProcesses.add(process);
            int exitValue;
            try {
                exitValue = process.waitFor();
                System.out.println("Process finished from: " + pb);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            result = exitValue;
            runningProcesses.remove(process);
        }

        inProcess.close();
        outProcess.close();
        errProcess.close();

        return result;
    }

    @Override
    public boolean supportsAnonPipeRead() {
        boolean result = processBuilders().stream().allMatch(IProcessBuilderCore::supportsAnonPipeRead);
        return result;
    }

    @Override
    public boolean supportsAnonPipeWrite() {
        boolean result = processBuilders().stream().allMatch(IProcessBuilderCore::supportsAnonPipeWrite);
        return result;
    }

    /** Groups only support direct named pipes if there is only a single member that accepts a direct named pipe. */
    @Override
    public boolean supportsDirectNamedPipe() {
        boolean result = processBuilders().stream().filter(IProcessBuilderCore::supportsDirectNamedPipe).count() <= 1;
        return result;
    }

    @Override
    public boolean accessesStdIn() {
        boolean result = processBuilders().stream().anyMatch(IProcessBuilderCore::accessesStdIn);
        return result;
    }
}
