package org.aksw.vshell.registry;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.lang.ProcessBuilder.Redirect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.aksw.shellgebra.exec.graph.JRedirect;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectJava;
import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.aksw.shellgebra.processbuilder.IProcessBuilderCore;
import org.aksw.shellgebra.processbuilder.ProcessBuilderBase;
import org.aksw.shellgebra.processbuilder.ProcessBuilderCoreNativeWrapper;
import org.aksw.vshell.registry.ProcessBase.OutboundIo;
import org.aksw.vshell.registry.ProcessBase.ToInternalIo;
import org.apache.commons.io.IOUtils;

public class ProcessBuilderNative
    extends ProcessBuilderBase<ProcessBuilderNative>
{
    public static ProcessBuilderNative of(String ...command) {
        return new ProcessBuilderNative().command(command);
    }

    public static ProcessBuilderNative of(List<String> command) {
        return new ProcessBuilderNative().command(command);
    }

    @Override
    public Process start(ProcessRunner cxt) throws IOException {
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(this.command());
        Process process = start(pb, this, cxt);
        return process;
    }

    public static Process start(ProcessBuilder pb, IProcessBuilderCore<?> self, ProcessRunner cxt) throws IOException {
        IProcessBuilderCore<?> pbWrapper = ProcessBuilderCoreNativeWrapper.wrap(pb);
        Process result = startInThread(pbWrapper, self, cxt);
        return result;
    }

    public static Process startInThread(IProcessBuilderCore<?> pb, IProcessBuilderCore<?> self, ProcessRunner cxt) throws IOException {
        pb.environment().putAll(self.environment());
        if (self.directory() != null) {
            pb.directory(self.directory());
        }

        boolean inheritInFromSystem = true;
        boolean inheritOutFromSystem = true;
        boolean inheritErrFromSystem = true;

        // ProcessBuilder clone = clone(processBuilder);
        // self.redirectOutput(new JRedirectJava(Redirect.to(new File("/tmp/test"))));
        DynamicInputShared din = configureInput(self.redirectInput(), cxt.internalIn(), inheritInFromSystem, pb::redirectInput);
        DynamicOutputShared dout = configureOutput(self.redirectOutput(), cxt.internalOut(), inheritOutFromSystem, pb::redirectOutput);
        DynamicOutputShared derr = configureOutput(self.redirectError(), cxt.internalErr(), inheritErrFromSystem, pb::redirectError);

        ToInternalIo internalIo = new ToInternalIo(din, dout, derr);

        OutboundIo outboundIo = ProcessShell.setupPublicStreams(self, cxt);
        CompletableFuture<Process> futureProcess = CompletableFuture.supplyAsync(() -> {
            try {
                Process r = pb.start();
                return r;
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
        Process rawProcess = ProcessOverFuture.ofOne(futureProcess);

        // Process rawProcess = pb.start();
        rawProcess.onExit().whenComplete((prcess, throwable) -> {
            internalIo.close();
        });
        Process result = ProcessShell.wrapIfNeeded(rawProcess, outboundIo);
        return result;
    }

    private static DynamicInputShared configureInput(JRedirect redirect, DynamicInputShared fd, boolean fdOverridesInherit, Consumer<Redirect> redirectConsumer) {
        DynamicInputShared result = null;
        if (redirect instanceof JRedirectJava x) {
            Redirect r = x.redirect();
            Type type = r.type();
            switch (type) {
            case PIPE:
                result = fd.dup();
                try {
                    redirectConsumer.accept(Redirect.from(result.getFile().toFile()));
                } catch (Exception e) {
                    IOUtils.closeQuietly(result);
                    throw new RuntimeException(e);
                }
                break;
            case READ:
                File file = r.file();
                result = DynamicInputShared.of(FileInput.of(file));
                redirectConsumer.accept(Redirect.from(file));
                break;
            case INHERIT:
                if (fdOverridesInherit) {
                    result = fd.dup();
                    try {
                        redirectConsumer.accept(Redirect.from(result.getFile().toFile()));
                    } catch (Exception e) {
                        IOUtils.closeQuietly(result);
                        throw new RuntimeException(e);
                    }
                }
                break;
            default:
                throw new RuntimeException("unsupported or not implemented");
                // nothing to do?
            }
        } else {
            throw new RuntimeException("unsupported or not implemented");
        }
        return result;
    }

    private static DynamicOutputShared configureOutput(JRedirect redirect, DynamicOutputShared fd, boolean fdOverridesInherit, Consumer<Redirect> redirectConsumer) {
        DynamicOutputShared result = null;
        if (redirect instanceof JRedirectJava x) {
            Redirect r = x.redirect();
            Type type = r.type();
            switch (type) {
            case PIPE:
                result = fd.dup();
                try {
                    redirectConsumer.accept(Redirect.to(fd.getFile().toFile()));
                } catch (Exception e) {
                    IOUtils.closeQuietly(result);
                    throw new RuntimeException(e);
                }
                break;
            case WRITE:
                File file = r.file();
                result = DynamicOutputShared.of(FileOutput.of(file));
                redirectConsumer.accept(Redirect.to(file));
                break;
            case INHERIT:
                if (fdOverridesInherit) {
                    result = fd.dup();
                    try {
                        redirectConsumer.accept(Redirect.to(fd.getFile().toFile()));
                    } catch (Exception e) {
                        IOUtils.closeQuietly(result);
                        throw new RuntimeException(e);
                    }
                }
                break;
            default:
                // nothing to do?
                throw new RuntimeException("unsupported or not implemented");
            }
        }
        return result;
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

    @Override
    public boolean accessesStdIn() {
        return true;
    }
}
