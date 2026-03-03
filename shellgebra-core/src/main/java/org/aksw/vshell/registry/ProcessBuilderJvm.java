package org.aksw.vshell.registry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.List;
import java.util.Objects;

import org.aksw.commons.util.docker.Argv;
import org.aksw.shellgebra.exec.graph.JRedirect;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectJava;
import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.aksw.shellgebra.processbuilder.ProcessBuilderBase;
import org.aksw.shellgebra.shim.core.JvmCommand;
import org.aksw.vshell.registry.ProcessBase.OutboundIo;

public class ProcessBuilderJvm
    extends ProcessBuilderBase<ProcessBuilderJvm>
{
    protected JvmCommandRegistry jvmCmdRegistry;

    public ProcessBuilderJvm() {
        super();
    }

    public static ProcessBuilderJvm of(JvmCommandRegistry jvmCmdRegistry, String ...argv) {
        return new ProcessBuilderJvm().setJvmCmdRegistry(jvmCmdRegistry).command(argv);
    }

    public static ProcessBuilderJvm of(JvmCommandRegistry jvmCmdRegistry, List<String> argv) {
        return new ProcessBuilderJvm().setJvmCmdRegistry(jvmCmdRegistry).command(argv);
    }

    public ProcessBuilderJvm setJvmCmdRegistry(JvmCommandRegistry jvmCmdRegistry) {
        this.jvmCmdRegistry = jvmCmdRegistry;
        return self();
    }

    @Override
    public boolean supportsAnonPipeRead() {
        return true;
    }

    @Override
    public boolean supportsAnonPipeWrite() {
        return true;
    }

    @Override
    public boolean supportsDirectNamedPipe() {
        return true;
    }

    @Override
    public boolean accessesStdIn() {
        return true;
    }

    protected static DynamicInput resolveInputRedirect(DynamicInputShared defaultSource, JRedirect redirect) throws FileNotFoundException {
        DynamicInput result;
        if (redirect instanceof JRedirectJava x) {
            Redirect r = x.redirect();
            switch (r.type()) {
            case INHERIT:
                result = defaultSource.dup(); // ClosePolicyWrapper.dontClose(defaultSource);
                break;
            case READ:
                result = FileInput.of(r.file()); // ClosePolicyWrapper.doClose(FileInput.of(r.file()));
                break;
            case PIPE:
                throw new RuntimeException("Unsupported or not implemented");
                // result =
                // break;
            default:
                throw new RuntimeException("Unsupported or not implemented");
            }
        } else {
            throw new RuntimeException("Unsupported or not implemented");
        }
        return result;
    }

    protected static DynamicOutput resolveOutputRedirect(DynamicOutputShared defaultSink, JRedirect redirect) throws FileNotFoundException {
        DynamicOutput result;
        if (redirect instanceof JRedirectJava x) {
            Redirect r = x.redirect();
            switch (r.type()) {
            case INHERIT:
                result = defaultSink.dup();
                // result = ClosePolicyWrapper.doClose(defaultTarget);
                break;
            case WRITE:
                result = FileOutput.of(r.file()); // ClosePolicyWrapper.doClose(FileOutput.of(r.file()));
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
        Objects.requireNonNull(jvmCmdRegistry, "jvmCmdRegistry");
        List<String> argvList = command();
        Argv a = Argv.of(argvList);
        String c = a.command();
        JvmCommand cmd = jvmCmdRegistry.get(c)
                .orElseThrow(() -> new RuntimeException("Command not found: " + c));

        OutboundIo outboundIo = ProcessShell.setupPublicStreams(this, executor);
        DynamicInput in = resolveInputRedirect(executor.internalIn(), redirectInput());
        DynamicOutput out = resolveOutputRedirect(executor.internalOut(), redirectOutput());
        DynamicOutput err = resolveOutputRedirect(executor.internalErr(), redirectError());

        JvmExecCxt execCxt = new JvmExecCxt(
                executor,
                jvmCmdRegistry,
                executor.environment(), executor.directory(), in, out, err);

        Process process = ProcessOverCompletableFuture.of(outboundIo, () -> runCommand(execCxt, jvmCmdRegistry, a, cmd));
        return process;
    }

    private Integer runCommand(JvmExecCxt execCxt, JvmCommandRegistry jvmCmdRegistry, Argv a, JvmCommand cmd) {
        // XXX Is ClosePolicyWrapper sufficient or is reference counting needed?
        // TODO Make the process expose the appropriate pipe
        int exitValue;
        try {
            exitValue = cmd.run(execCxt, a);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                execCxt.in().close();
                execCxt.out().close();
                execCxt.err().close();
            } catch (Exception e2) {
                throw new RuntimeException(e2);
            }
        }
        return exitValue;
    }

    @Override
    protected ProcessBuilderJvm cloneActual() {
        return new ProcessBuilderJvm().setJvmCmdRegistry(jvmCmdRegistry);
    }
}
