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

    public static ProcessBuilderJvm of(List<String> argv) {
        return new ProcessBuilderJvm().command(argv);
    }

    @Override
    public boolean supportsAnonPipeRead() {
        return true;
    }

    @Override
    public boolean supportsAnonPipeWrite() {
        return true;
    }

    protected static ClosePolicyWrapper<FileInput> resolveInputRedirect(FileInput defaultSource, JRedirect redirect) throws FileNotFoundException {
        ClosePolicyWrapper<FileInput> result;
        if (redirect instanceof JRedirectJava x) {
            Redirect r = x.redirect();
            switch (r.type()) {
            case INHERIT:
                result = ClosePolicyWrapper.dontClose(defaultSource);
                break;
            case READ:
                result = ClosePolicyWrapper.doClose(FileInput.of(r.file()));
                break;
            default:
                throw new RuntimeException("Unsupported or not implemented");
            }
        } else {
            throw new RuntimeException("Unsupported or not implemented");
        }
        return result;
    }

    protected static ClosePolicyWrapper<FileOutput> resolveOutputRedirect(FileOutput defaultTarget, JRedirect redirect) throws FileNotFoundException {
        ClosePolicyWrapper<FileOutput> result;
        if (redirect instanceof JRedirectJava x) {
            Redirect r = x.redirect();
            switch (r.type()) {
            case INHERIT:
                result = ClosePolicyWrapper.dontClose(defaultTarget);
                break;
            case WRITE:
                result = ClosePolicyWrapper.doClose(FileOutput.of(r.file()));
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
        Process process = ProcessOverCompletableFuture.of(() -> runCommand(executor, a, cmd));
        return process;
    }

    private Integer runCommand(ProcessRunner executor, Argv a, JvmCommand cmd) {
        // XXX Is ClosePolicyWrapper sufficient or is reference counting needed?
        try(
            ClosePolicyWrapper<FileInput> in = resolveInputRedirect(executor.internalIn(), redirectInput());
            ClosePolicyWrapper<FileOutput> out = resolveOutputRedirect(executor.internalOut(), redirectOutput());
            ClosePolicyWrapper<FileOutput> err = resolveOutputRedirect(executor.internalErr(), redirectError())) {

            JvmExecCxt execCxt = new JvmExecCxt(
                executor,
                executor.environment(), executor.directory(), in.entity(), out.entity(), err.entity());

            int exitValue = cmd.run(execCxt, a);
            return exitValue;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected ProcessBuilderJvm cloneActual() {
        return new ProcessBuilderJvm();
    }
}
