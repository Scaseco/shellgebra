package org.aksw.vshell.registry;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import org.aksw.shellgebra.exec.graph.PosixPipe;

public class DynamicOutputFromPipe
    extends OutputBase
    implements DynamicOutput
{
    private PosixPipe pipe;

    protected DynamicOutputFromPipe(PosixPipe pipe) {
        super(pipe.getOutputStream());
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
        return pipe.getOutputStream();
    }
}
