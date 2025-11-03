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

    // XXX Add support to get an input stream AND an underlying exit code!

    // XXX Perhaps add support for OutputStream destinations? Sketch:
    // BoundStage writeTo(Stage nextStage); -- this is the same as setting this BoundStage as an input for the next one.
    // Task writeTo(OutputStream out);  -- this should write the output - probably there is no further object we could follow up with.
    // Task has methods start, abort, close().

    // XXX Add support to just execute (without piping output to a sink)
    // CompletableFuture<?> execute();
}
