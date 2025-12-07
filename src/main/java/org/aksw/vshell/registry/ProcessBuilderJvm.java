package org.aksw.vshell.registry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.List;

import org.aksw.commons.util.docker.Argv;
import org.aksw.shellgebra.exec.ProcessBuilderBase;
import org.aksw.shellgebra.exec.graph.JRedirect;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectJava;
import org.aksw.shellgebra.exec.graph.ProcessRunner;

public class ProcessBuilderJvm
    extends ProcessBuilderBase<ProcessBuilderJvm>
{
    public ProcessBuilderJvm() {
        super();
    }

    public static ProcessBuilderJvm of(String ...argv) {
        return new ProcessBuilderJvm().command(argv);
    }

    protected record ClosePolicyWrapper<T>(T entity, AutoCloseable closeAction, boolean doClose) implements AutoCloseable {
        public static <T extends AutoCloseable> ClosePolicyWrapper<T> doClose(T entity) {
            return new ClosePolicyWrapper<>(entity, entity, true);
        }

        public static <T extends AutoCloseable> ClosePolicyWrapper<T> dontClose(T entity) {
            return new ClosePolicyWrapper<>(entity, entity, false);
        }

        @Override
        public void close() throws Exception {
            if (doClose && closeAction != null) {
                closeAction.close();
            }
        }
    }

    protected static ClosePolicyWrapper<FileInputSource> resolveInputRedirect(FileInputSource defaultSource, JRedirect redirect) throws FileNotFoundException {
        ClosePolicyWrapper<FileInputSource> result;
        if (redirect instanceof JRedirectJava x) {
            Redirect r = x.redirect();
            switch (r.type()) {
            case INHERIT:
                result = ClosePolicyWrapper.dontClose(defaultSource);
                break;
            case READ:
                result = ClosePolicyWrapper.doClose(FileInputSource.of(r.file()));
                break;
            default:
                throw new RuntimeException("Unsupported or not implemented");
            }
        } else {
            throw new RuntimeException("Unsupported or not implemented");
        }
        return result;
    }

    protected static ClosePolicyWrapper<FileOutputTarget> resolveOutputRedirect(FileOutputTarget defaultTarget, JRedirect redirect) throws FileNotFoundException {
        ClosePolicyWrapper<FileOutputTarget> result;
        if (redirect instanceof JRedirectJava x) {
            Redirect r = x.redirect();
            switch (r.type()) {
            case INHERIT:
                result = ClosePolicyWrapper.dontClose(defaultTarget);
                break;
            case WRITE:
                result = ClosePolicyWrapper.doClose(FileOutputTarget.of(r.file()));
                break;
            default:
                throw new RuntimeException("Unsupported or not implemented");
            }
        } else {
            throw new RuntimeException("Unsupported or not implemented");
        }
        return result;
    }

    @Override
    public Process start(ProcessRunner executor) throws IOException {
        List<String> argvList = command();
        Argv a = Argv.of(argvList);
        String c = a.command();
        JvmCommand cmd = executor.getJvmCmdRegistry().get(c)
                .orElseThrow(() -> new RuntimeException("Command not found: " + c));
        Process process = ProcessOverCompletableFuture.of(() -> {
            // Wire up the redirects.
            // XXX Use managed resources?
            try(
                ClosePolicyWrapper<FileInputSource> in = resolveInputRedirect(executor.internalIn(), redirectInput());
                ClosePolicyWrapper<FileOutputTarget> out = resolveOutputRedirect(executor.internalOut(), redirectOutput());
                ClosePolicyWrapper<FileOutputTarget> err = resolveOutputRedirect(executor.internalErr(), redirectError())) {

                JvmExecCxt execCxt = new JvmExecCxt(
                    executor,
                    executor.environment(), executor.directory(), in.entity(), out.entity(), err.entity());

                // Design question: what if a command is run with a redirect that starts a child process?
                // The usual logic is to create a copy of the fd table, so it would have to share stdin.
                // This means, that input from stdin would have to be shared.
                // And this means, that the resource has to be managed / reference counted.

                // One further issue is how to turn java streams into anonymous pipes on demand.
                // JvmExecCxt execCxt = new JvmExecCxt(null, null, null, in, null, null)

                int exitValue = cmd.run(execCxt, a);


                return exitValue;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return process;
    }

    @Override
    protected ProcessBuilderJvm cloneActual() {
        return new ProcessBuilderJvm();
    }
}
