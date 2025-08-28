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
}
