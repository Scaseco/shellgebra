package org.aksw.shellgebra.io.pipe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.aksw.vshell.registry.Input;
import org.aksw.vshell.registry.InputBase;
import org.aksw.vshell.registry.Output;
import org.aksw.vshell.registry.OutputBase;

public class JavaPipe
    extends PipeBase
{
    private PipedOutputStream out;
    private PipedInputStream in;

    private Input input;
    private Output output;

    public Input input() {
        return input;
    }

    public Output output() {
        return output;
    }

    private JavaPipe() {
        this.out = new PipedOutputStream();
        try {
            this.in = new PipedInputStream(out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.input = new InputBase(null) {
            @Override
            protected InputStream openInputStream() throws IOException {
                return in;
            }
        };
        this.output = new OutputBase(null) {
            @Override
            protected OutputStream openOutputStream() throws IOException {
                return out;
            }
        };
    }

    public static JavaPipe create() {
        return new JavaPipe();
    }
}
