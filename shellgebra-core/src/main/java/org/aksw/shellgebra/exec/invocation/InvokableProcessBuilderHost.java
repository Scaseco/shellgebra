package org.aksw.shellgebra.exec.invocation;

import java.io.IOException;
import java.util.List;

import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.aksw.vshell.registry.ProcessBuilderNative;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvokableProcessBuilderHost
    extends InvokableProcessBuilderBase<InvokableProcessBuilderHost>
{
    private static final Logger logger = LoggerFactory.getLogger(InvokableProcessBuilderHost.class);

    /** If unset then fall back to {@link InvocationCompilerImpl#getDefault()}. */
    private InvocationCompiler compiler = null;

    public static InvokableProcessBuilderHost of(List<String> args) {
        return new InvokableProcessBuilderHost().command(args);
    }

    @Override
    protected InvokableProcessBuilderHost cloneActual() {
        return new InvokableProcessBuilderHost();
    }

    public InvokableProcessBuilderHost compiler(InvocationCompiler compiler) {
        this.compiler = compiler;
        return self();
    }

    public InvocationCompiler compiler() {
        return compiler;
    }

    @Override
    public Process start(ProcessRunner executor) throws IOException {
        CompileContext ctx = CompileContext.noResolve();

        Invocation inv = invocation();
        if (inv == null) {
            throw new IllegalStateException("No invocation set");
        }

        InvocationCompiler finalCompiler = compiler != null ? compiler : InvocationCompilerImpl.getDefault();
        ExecutableInvocation exec = finalCompiler.compile(inv, ctx);
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(exec.argv());
        ProcessBuilderNative.configure(pb, this, executor);
        Process p = pb.start();
        // Cleanup after process exit.
        p.toHandle().onExit().thenRun(() -> {
            try {
                exec.close();
            } catch (Exception e) {
                logger.warn("Error during close", e);
            }
        });
        return p;
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
}
