package org.aksw.vshell.registry;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.aksw.shellgebra.exec.graph.PosixPipe;

public class DynamicInputFromPipe
    extends InputBase
    implements DynamicInput
{
    private PosixPipe pipe;

    public DynamicInputFromPipe(PosixPipe pipe) {
        super(pipe.getInputStream());
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
        return pipe.getInputStream();
    }
}
