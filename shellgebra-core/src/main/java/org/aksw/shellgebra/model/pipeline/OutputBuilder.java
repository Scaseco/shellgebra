package org.aksw.shellgebra.model.pipeline;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.Future;

public interface OutputBuilder {
    // ByteSource toByteSource();
    InputStream toInputStream();
    Future<Integer> toPath(Path outFile);

    ExecSpec toExecSpec();
}
