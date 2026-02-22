package org.aksw.shellgebra.io.pipe;

import java.io.File;
import java.nio.file.Path;

import org.aksw.vshell.registry.DynamicInput;
import org.aksw.vshell.registry.DynamicOutput;

// XXX Perhaps rename to file-based pipe or file-supported pipe.
//   The "dynamic" aspect means that there can be a file if needed.
//   The file (file descriptors) may only be allocated on demand.
public interface DynamicPipe
    extends Pipe
{
    @Override
    DynamicInput input();

    @Override
    DynamicOutput output();

    default Path getReadEndProcPath() {
        try {
            return input().getFile();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    default File getReadEndProcFile() {
        return getReadEndProcPath().toFile();
    }

    default Path getWriteEndProcPath() {
        try {
            return output().getFile();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    default File getWriteEndProcFile() {
        return getWriteEndProcPath().toFile();
    }
}
