package org.aksw.shellgebra.exec.graph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;

import org.aksw.shellgebra.exec.IProcessBuilder;
import org.aksw.vshell.registry.DynamicInput;
import org.aksw.vshell.registry.DynamicOutput;

public interface ProcessRunner
    extends AutoCloseable
{
    Map<String, String> environment();
    Path directory();

    DynamicInput internalIn();
    DynamicOutput internalOut();
    DynamicOutput internalErr();

    OutputStream getOutputStream();
    InputStream getInputStream();
    InputStream getErrorStream();

    default Path inputPipe() {
        try {
            return internalIn().getFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    default Path outputPipe() {
        try {
            return internalOut().getFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    default Path errorPipe() {
        try {
            return internalErr().getFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    IProcessBuilder<?> configure(IProcessBuilder<?> processBuilder);

    /** Close this runner for new processes, immediately close all process-facing pipes. */
    void shutdown() throws IOException;

    static void readLines(InputStream in, Charset charset, Consumer<String> lineCallback) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, charset))) {
            br.lines().forEach(lineCallback::accept);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
