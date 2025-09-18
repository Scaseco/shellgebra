package org.aksw.shellgebra.exec;

import java.nio.file.Path;

import org.aksw.shellgebra.exec.FileWriterTaskBase.PathLifeCycle;

import com.google.common.io.ByteSource;

public interface BoundStage
{
    ByteSource toByteSource();

    FileWriterTask execToRegularFile(Path hostPath);
    FileWriterTask execToFile(Path hostPath, PathLifeCycle pathLifeCycle);
    FileWriterTask runToHostPipe();

    // XXX Perhaps add support for OutputStream destinations? Sketch:
    // BoundStage writeTo(Stage nextStage);
    // BoundStage writeTo(OutputStream out);

    // XXX Add support to just execute (without piping output to a sink)
    // CompletableFuture<?> execute();
}
