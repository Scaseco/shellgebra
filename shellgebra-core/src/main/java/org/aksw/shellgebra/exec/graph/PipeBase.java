package org.aksw.shellgebra.exec.graph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

import org.aksw.vshell.registry.Input;
import org.aksw.vshell.registry.InputBase;
import org.aksw.vshell.registry.Output;
import org.aksw.vshell.registry.OutputBase;

public abstract class PipeBase {
    protected abstract OutputStream getOutputStream();
    protected abstract InputStream getInputStream();

    private Input input = new InputBase(null) {
        @Override
        protected InputStream openInputStream() throws IOException {
            return PipeBase.this.getInputStream();
        }
    };

    private Output output = new OutputBase(null) {
        @Override
        protected OutputStream openOutputStream() throws IOException {
            return PipeBase.this.getOutputStream();
        }
    };

    /*
     * Convenience methods below, inspired by ProcessBuilder from Java 17+.
     */

    public final PrintStream printer() {
        return output.printStream();
    }

    public final PrintStream printer(Charset charset) {
        return output.printStream(charset);
    }

    public final BufferedWriter writer() {
        return output.writer();
    }

    public final BufferedWriter writer(Charset charset) {
        return output.writer(charset);
    }

    public final BufferedReader reader() {
        return input.reader();
    }

    public final BufferedReader reader(Charset charset) {
        return input.reader(charset);
    }
}
