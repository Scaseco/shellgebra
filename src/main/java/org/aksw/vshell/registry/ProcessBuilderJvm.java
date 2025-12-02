package org.aksw.vshell.registry;

import java.io.IOException;

import org.aksw.commons.util.docker.Argv;
import org.aksw.shellgebra.exec.ProcessBuilderBase;
import org.aksw.shellgebra.exec.graph.ProcessRunner;

public class ProcessBuilderJvm
    extends ProcessBuilderBase<ProcessBuilderJvm>
{
    // protected ProcessRunner executor;

    public ProcessBuilderJvm() {
        super();
        // this.executor = executor;
    }

    public static ProcessBuilderJvm of(String ...argv) {
        return new ProcessBuilderJvm().command(argv);
    }

    @Override
    public Process start() throws IOException {
        throw new UnsupportedOperationException();
    }

    // @Override
    public Process start(ProcessRunner executor) throws IOException {
        Argv a = new Argv(command());
        String command = a.command();
        JvmCommand cmd = executor.getJvmCmdRegistry().get(command)
                .orElseThrow(() -> new RuntimeException("Command not found: " + command));
        Process process = ProcessOverCompletableFuture.of(() -> {
            int exitValue = cmd.run(executor, a);
            return exitValue;
        });
        return process;
    }
}

// protected JvmCommandExecutorImpl executor;
// OutputStream stdin = OutputStream.nullOutputStream();
// InputStream stdin = InputStream.nullInputStream();
// JvmContext context = executor.getContext();
//JvmExecCxt execCxt = new JvmExecCxt(
//context,
//// a.argv(),
//context.getEnvironment(),
//context.getDirectory(),
//process.getStdinIn(),
//new PrintStream(process.getStdoutSink()),
//new PrintStream(process.getStderrSink())
//);
//
//JvmExecCxt execCxt = new JvmExecCxt(
//context,
//// a.argv(),
//context.getEnvironment(),
//context.getDirectory(),
//process.getStdinIn(),
//process.getStdoutSink(),
//process.getStderrSink())
//);


// CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> cmd.run(executor, a));
//
//
//Thread thread = new Thread(() -> {
//int exitValue = cmd.run(executor, a);
//process.setExitValue(exitValue);
//});
//process.setThread(thread);
