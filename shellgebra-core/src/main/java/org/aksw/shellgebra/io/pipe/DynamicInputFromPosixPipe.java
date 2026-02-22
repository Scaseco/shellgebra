package org.aksw.shellgebra.io.pipe;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.aksw.vshell.registry.DynamicInput;
import org.aksw.vshell.registry.InputBase;

public class DynamicInputFromPosixPipe
    extends InputBase
    implements DynamicInput
{
    private PosixPipe pipe;

    public DynamicInputFromPosixPipe(PosixPipe pipe) {
        super(pipe.in);
        this.pipe = pipe;
    }

    @Override
    public boolean hasFile() {
        return true;
    }

    @Override
    public Path getFile() throws IOException {
        return pipe.getReadEndProcPath();
    }

    @Override
    protected InputStream openInputStream() throws IOException {
        throw new IllegalStateException("Should not be called");
        // return pipe.in;
    }
}
