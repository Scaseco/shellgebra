package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.nio.file.Path;

public class FileWriterTaskFromContainer
    extends FileWriterTaskViaExecutor
{
    // protected

    public FileWriterTaskFromContainer(Path path, PathLifeCycle pathLifeCycle) {
        super(path, pathLifeCycle);
    }

    @Override
    protected void abortActual() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void prepareWriteFile() throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void runWriteFile() throws IOException {
        // TODO Auto-generated method stub

    }

}
