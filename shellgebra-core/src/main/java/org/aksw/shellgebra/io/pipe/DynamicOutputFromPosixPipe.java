package org.aksw.shellgebra.io.pipe;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import org.aksw.vshell.registry.DynamicOutput;
import org.aksw.vshell.registry.OutputBase;

public class DynamicOutputFromPosixPipe
    extends OutputBase
    implements DynamicOutput
{
    private PosixPipe pipe;

    public DynamicOutputFromPosixPipe(PosixPipe pipe) {
        super(pipe.out);
        this.pipe = pipe;
    }

    @Override
    public boolean hasFile() {
        return true;
    }

    @Override
    public Path getFile() throws IOException {
        return pipe.getWriteEndProcPath();
    }

    @Override
    protected OutputStream openOutputStream() throws IOException {
        throw new IllegalStateException("Should not be called");
        // return pipe.out;
    }
}
