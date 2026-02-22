package org.aksw.vshell.registry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.lang.ProcessBuilder.Redirect.Type;
import java.nio.file.Path;
import java.util.function.Supplier;

import org.aksw.shellgebra.exec.graph.JRedirect;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectJava;
import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.aksw.shellgebra.processbuilder.IProcessBuilderCore;
import org.aksw.vshell.registry.ProcessBase.OutboundIo;
import org.apache.commons.io.IOUtils;

public class ProcessShell
    extends ProcessWrapper<Process> {

    private Output toIn;
    private Input fromOut;
    private Input fromErr;

    public ProcessShell(Process delegate, Output toIn, Input fromOut, Input fromErr) {
        super(delegate);
        this.toIn = toIn;
        this.fromOut = fromOut;
        this.fromErr = fromErr;
    }

    protected Process of(Process delegate, Path toInPath, Path fromOutPath, Path fromErrPath) {
        Output toIn = FileOutput.of(toInPath);
        Input fromOut = FileInput.of(fromOutPath);
        Input fromErr = FileInput.of(fromErrPath);
        return wrap(delegate, toIn, fromOut, fromErr);
    }

    public static Process wrap(Process delegate, OutputStream toInStream, InputStream fromOutStream, InputStream fromErrStream) {
        Output toIn = OutputBase.of(toInStream);
        Input fromOut = InputBase.of(fromOutStream);
        Input fromErr = InputBase.of(fromErrStream);
        return wrap(delegate, toIn, fromOut, fromErr);
    }

    public static Process wrap(Process delegate, Output toIn, Input fromOut, Input fromErr) {
        try {
            boolean eager = true;
            if (eager) {
                toIn.outputStream();
                fromOut.inputStream();
                fromErr.inputStream();
            }
//            toIn = toInPath == null ? null : Files.newOutputStream(toInPath);
//            fromOut = fromOutPath == null ? null : Files.newInputStream(fromOutPath);
//            fromErr = fromErrPath == null ? null : Files.newInputStream(fromErrPath);
        } catch (Exception e) {
            IOUtils.closeQuietly(toIn);
            IOUtils.closeQuietly(fromOut);
            IOUtils.closeQuietly(fromErr);
            e.addSuppressed(new RuntimeException("Passed through here."));
            throw e;
        }
        return new ProcessShell(delegate, toIn, fromOut, fromErr);
    }

    @Override
    public OutputStream getOutputStream() {
        return toIn != null ? toIn.outputStream() : super.getOutputStream();
    }

    @Override
    public InputStream getInputStream() {
        return fromOut != null ? fromOut.inputStream() : super.getInputStream();
    }

    @Override
    public InputStream getErrorStream() {
        return fromErr != null ? fromErr.inputStream() : super.getErrorStream();
    }

    public static OutboundIo setupPublicStreams(IProcessBuilderCore<?> pb, ProcessRunner cxt) throws IOException {
        boolean inheritInFromSystem = true;
        boolean inheritOutFromSystem = true;
        boolean inheritErrFromSystem = true;
        OutputStream toIn = configureInput(pb.redirectInput(), inheritInFromSystem, () -> cxt.getOutputStream());
        InputStream fromOut = configureOutput(pb.redirectOutput(), inheritOutFromSystem, () -> cxt.getInputStream());
        InputStream fromErr = configureOutput(pb.redirectError(), inheritErrFromSystem, () -> cxt.getErrorStream());
        return new OutboundIo(toIn, fromOut, fromErr);
    }

    public static Process wrapIfNeeded(Process rawProcess, OutboundIo streams) {
        OutputStream toIn = streams.toIn();
        InputStream fromOut = streams.fromOut();
        InputStream fromErr = streams.fromErr();
        // Only use the context outward-facing streams for redirects of type INHERIT or PIPE
        Process result = (streams.toIn() != null || fromOut != null || fromErr != null)
            ? ProcessShell.wrap(rawProcess, toIn, fromOut, fromErr)
            : rawProcess;
        return result;
    }

    private static OutputStream configureInput(JRedirect redirect, boolean fdOverridesInherit, Supplier<OutputStream> toInFn) {
        OutputStream toIn = null;
        if (redirect instanceof JRedirectJava x) {
            Redirect r = x.redirect();
            Type type = r.type();
            switch (type) {
            case PIPE:
                toIn = toInFn.get();
                break;
            case READ:
                break;
            case INHERIT:
                if (fdOverridesInherit) {
                    toIn = toInFn.get();
                }
                break;
            default:
                throw new RuntimeException("unsupported or not implemented");
                // nothing to do?
            }
        } else {
            throw new RuntimeException("unsupported or not implemented");
        }
        return toIn;
    }

    private static InputStream configureOutput(JRedirect redirect, boolean fdOverridesInherit, Supplier<InputStream> fromOutFn) {
        InputStream fromOut = null;
        if (redirect instanceof JRedirectJava x) {
            Redirect r = x.redirect();
            Type type = r.type();
            switch (type) {
            case PIPE:
                fromOut = fromOutFn.get();
                break;
            case WRITE:
                break;
            case INHERIT:
                if (fdOverridesInherit) {
                    fromOut = fromOutFn.get();
                }
                break;
            default:
                // nothing to do?
                throw new RuntimeException("unsupported or not implemented");
            }
        }
        return fromOut;
    }
}
