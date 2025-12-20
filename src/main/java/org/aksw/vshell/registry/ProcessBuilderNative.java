package org.aksw.vshell.registry;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.lang.ProcessBuilder.Redirect.Type;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.aksw.shellgebra.exec.IProcessBuilderCore;
import org.aksw.shellgebra.exec.ProcessBuilderBase;
import org.aksw.shellgebra.exec.graph.JRedirect;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectJava;
import org.aksw.shellgebra.exec.graph.ProcessRunner;

public class ProcessBuilderNative
    extends ProcessBuilderBase<ProcessBuilderNative>
{
    public static ProcessBuilderNative of(String ...command) {
        return new ProcessBuilderNative().command(command);
    }

    @Override
    public Process start(ProcessRunner executor) throws IOException {
        ProcessBuilder pb = new ProcessBuilder();

        pb.command(this.command());
        pb.environment().putAll(this.environment());
        if (this.directory() != null) {
            pb.directory(this.directory().toFile());
        }

        pb = configure(pb, this, executor);

        // pb = executor.configure(pb);

        return pb.start();
    }


    private static void configureInput(JRedirect redirect, Path fd, boolean fdOverridesInherit, Consumer<Redirect> redirectConsumer) {
        if (redirect instanceof JRedirectJava x) {
            Redirect r = x.redirect();
            Type type = r.type();
            switch (type) {
            case PIPE:
                redirectConsumer.accept(Redirect.from(fd.toFile()));
                break;
            case READ:
                redirectConsumer.accept(Redirect.from(r.file()));
                break;
            case INHERIT:
                if (fdOverridesInherit) {
                    redirectConsumer.accept(Redirect.from(fd.toFile()));
                }
                break;
            default:
                throw new RuntimeException("unsupported or not implemented");
                // nothing to do?
            }
        } else {
            throw new RuntimeException("unsupported or not implemented");
        }
    }

    private static void configureOutput(JRedirect redirect, Path fd, boolean fdOverridesInherit, Consumer<Redirect> redirectConsumer) {
        if (redirect instanceof JRedirectJava x) {
            Redirect r = x.redirect();
            Type type = r.type();
            switch (type) {
            case PIPE:
                redirectConsumer.accept(Redirect.to(fd.toFile()));
                break;
            case WRITE:
                redirectConsumer.accept(Redirect.to(r.file()));
                break;
            case INHERIT:
                if (fdOverridesInherit) {
                    redirectConsumer.accept(Redirect.to(fd.toFile()));
                }
                break;
            default:
                // nothing to do?
                throw new RuntimeException("unsupported or not implemented");
            }
        }
    }

//    private ProcessBuilder configure(ProcessBuilder pb, ProcessRunner cxt) {
//        boolean inheritInFromSystem = true;
//        boolean inheritOutFromSystem = true;
//        boolean inheritErrFromSystem = true;
//
//        // ProcessBuilder clone = clone(processBuilder);
//        configureInput(redirectInput(), cxt.inputPipe(), inheritInFromSystem, pb::redirectInput);
//        configureOutput(redirectOutput(), cxt.outputPipe(), inheritOutFromSystem, pb::redirectOutput);
//        configureOutput(redirectError(), cxt.errorPipe(), inheritErrFromSystem, pb::redirectError);
//        return pb;
//    }

    public static  ProcessBuilder configure(ProcessBuilder tgt, IProcessBuilderCore<?> src, ProcessRunner cxt) {
        boolean inheritInFromSystem = true;
        boolean inheritOutFromSystem = true;
        boolean inheritErrFromSystem = true;

        // ProcessBuilder clone = clone(processBuilder);
        configureInput(src.redirectInput(), cxt.inputPipe(), inheritInFromSystem, tgt::redirectInput);
        configureOutput(src.redirectOutput(), cxt.outputPipe(), inheritOutFromSystem, tgt::redirectOutput);
        configureOutput(src.redirectError(), cxt.errorPipe(), inheritErrFromSystem, tgt::redirectError);
        return tgt;
    }

    @Override
    protected ProcessBuilderNative cloneActual() {
        return new ProcessBuilderNative();
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
}
