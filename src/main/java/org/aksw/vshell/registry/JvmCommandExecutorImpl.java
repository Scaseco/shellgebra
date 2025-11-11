package org.aksw.vshell.registry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;

import org.aksw.commons.util.docker.Argv;
import org.aksw.shellgebra.exec.IProcessBuilder;
import org.apache.commons.exec.ExecuteException;

public class JvmCommandExecutorImpl
    implements JvmCommandExecutor
{
    private JvmContext context;

    public JvmCommandExecutorImpl(JvmContext context) {
        super();
        this.context = context;
    }

    public JvmContext getContext() {
        return context;
    }

    protected JvmCommand resolveCommand(String name) {
        String match = which(name);
        JvmCommand cmd = context.getJvmCmdRegistry().get(match).orElseThrow();
        return cmd;

    }

    protected String which(String name) {
        JvmContext cxt = getContext();
        List<String> pathEntries = PathResolutionUtils.getPathItems(cxt.getEnvironment(), "PATH", ":");
        List<String> matches = JvmCommandWhich.resolve(context.getJvmCmdRegistry(), pathEntries, name, 1);
        if (matches.isEmpty()) {
            throw new NoSuchElementException("No command with name " + name);
        }
        String result = matches.get(0);
        return result;
    }


    public int run(String...argv) {
        JvmContext context = getContext();
        Argv a = Argv.of(argv);
        JvmCommand cmd = resolveCommand(a.command());

        // OutputStream stdin = OutputStream.nullOutputStream();
        InputStream stdin = InputStream.nullInputStream();

        JvmExecCxt execCxt = new JvmExecCxt(
            context,
            // a.argv(),
            context.getEnvironment(),
            context.getDirectory(),
            stdin,
            context.getOut(),
            context.getErr()
        );

        int exitValue = cmd.run(execCxt, a);
        return exitValue;
    }

    @Override
    public IProcessBuilder<?> newProcessBuilder(String... argv) {
        return new ProcessBuilderJvm(this);
    }

    @Override
    public String exec(String... argv) throws IOException {
        JvmContext context = getContext();
        Argv a = Argv.of(argv);
        JvmCommand cmd = resolveCommand(a.command());

        // OutputStream stdin = OutputStream.nullOutputStream();
        InputStream stdin = InputStream.nullInputStream();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream out = new PrintStream(baos)) {
            JvmExecCxt execCxt = new JvmExecCxt(
                context,
                // a.argv(),
                context.getEnvironment(),
                context.getDirectory(),
                stdin,
                out,
                context.getErr()
            );
            int exitValue = cmd.run(execCxt, a);
            if (exitValue != 0) {
                throw new ExecuteException("Non zero exit code: " + exitValue, exitValue);
            }

            baos.flush();
            String str = baos.toString(StandardCharsets.UTF_8);
            str = JvmExecUtils.removeTrailingNewline(str);
            return str;
        }
    }
}
