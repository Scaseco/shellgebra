package org.aksw.shellgebra.exec.graph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;

import org.aksw.shellgebra.exec.IProcessBuilder;
import org.aksw.vshell.registry.JvmCommandRegistry;
import org.aksw.vshell.registry.ProcessBuilderJvm;

public interface ProcessRunner
    extends AutoCloseable
{
    JvmCommandRegistry getJvmCmdRegistry();
    Map<String, String> environment();

    Path inputPipe();
    Path outputPipe();
    Path errorPipe();

    InputStream internalIn();
    OutputStream internalOut();

    OutputStream internalErr();
    PrintStream internalPrintOut();
    PrintStream internalPrintErr();

    Thread setOutputReader(Consumer<InputStream> reader);

    default Thread setOutputLineReaderUtf8(Consumer<String> lineCallback) {
        return setOutputLineReader(StandardCharsets.UTF_8, lineCallback);
    }

    default Thread setOutputLineReader(Charset charset, Consumer<String> lineCallback) {
        return setOutputReader(in -> readLines(in, charset, lineCallback));
    }

    Thread setErrorReader(Consumer<InputStream> reader);

    default Thread setErrorLineReaderUtf8(Consumer<String> lineCallback) {
        return setErrorLineReader(StandardCharsets.UTF_8, lineCallback);
    }

    default Thread setErrorLineReader(Charset charset, Consumer<String> lineCallback) {
        return setErrorReader(in -> readLines(in, charset, lineCallback));
    }

    Thread setInputGenerator(Consumer<OutputStream> inputSupplier);

    default Thread setInputPrintStreamUtf8(Consumer<PrintStream> writerCallback) {
        return setInputPrintStream(StandardCharsets.UTF_8, true, writerCallback);
    }

    default Thread setInputPrintStream(Charset charset, boolean autoFlush, Consumer<PrintStream> writerCallback) {
        return setInputGenerator(out -> writerCallback.accept(new PrintStream(out, autoFlush, charset)));
    }

    OutputStream getOutputStream();
    InputStream getInputStream();
    InputStream getErrorStream();

    ProcessBuilder configure(ProcessBuilder processBuilder);
    IProcessBuilder<?> configure(IProcessBuilder<?> processBuilder);

    /** Does not alter the provided process builder. */
    Process start(ProcessBuilder nativeProcessBuilder) throws IOException;
    Process startJvm(ProcessBuilderJvm jvmProcessBuilder);

    static void readLines(InputStream in, Charset charset, Consumer<String> lineCallback) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, charset))) {
            br.lines().forEach(lineCallback::accept);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
