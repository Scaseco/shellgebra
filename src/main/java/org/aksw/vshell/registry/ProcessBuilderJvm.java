package org.aksw.vshell.registry;

import java.io.IOException;
import java.io.PrintStream;

import org.aksw.commons.util.docker.Argv;
import org.aksw.shellgebra.exec.ProcessBuilderBase;

public class ProcessBuilderJvm
    extends ProcessBuilderBase<ProcessBuilderJvm>
{
    protected JvmCommandExecutorImpl executor;

    public ProcessBuilderJvm(JvmCommandExecutorImpl executor) {
        super();
        this.executor = executor;
    }

    @Override
    public Process build() throws IOException {
        JvmContext context = executor.getContext();
        Argv a = new Argv(command());
        String command = a.command();
        JvmCommand cmd = context.getJvmCmdRegistry().get(command).orElse(null);

        // OutputStream stdin = OutputStream.nullOutputStream();
        // InputStream stdin = InputStream.nullInputStream();

        ProcessOverThread process = new ProcessOverThread();
        JvmExecCxt execCxt = new JvmExecCxt(
            context,
            // a.argv(),
            context.getEnvironment(),
            context.getDirectory(),
            process.getStdinIn(),
            new PrintStream(process.getStdoutSink()),
            new PrintStream(process.getStderrSink())
        );
//
//        JvmExecCxt execCxt = new JvmExecCxt(
//            context,
//            // a.argv(),
//            context.getEnvironment(),
//            context.getDirectory(),
//            process.getStdinIn(),
//            process.getStdoutSink(),
//            process.getStderrSink())
//        );


        Thread thread = new Thread(() -> {
            int exitValue = cmd.run(execCxt, a);
            process.setExitValue(exitValue);
        });
        process.setThread(thread);
        return process;
    }
}
