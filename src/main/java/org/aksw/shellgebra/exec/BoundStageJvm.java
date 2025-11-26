package org.aksw.shellgebra.exec;

import java.nio.file.Path;
import java.util.Objects;

import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.unused.algebra.plan.InputStreamTransform;

import com.google.common.io.ByteSource;

public class BoundStageJvm
    implements BoundStage
{
    private ByteSource byteSource;
    private InputStreamTransform transform;

    public BoundStageJvm(ByteSource byteSource, InputStreamTransform transform) {
        super();
        this.byteSource = Objects.requireNonNull(byteSource);
        this.transform = Objects.requireNonNull(transform);
    }

    @Override
    public ByteSource toByteSource() {
        return TransformedByteSource.transform(byteSource, transform);
    }

    @Override
    public FileWriterTask execToRegularFile(Path hostPath) {
        ByteSource bs = toByteSource();
        return new FileWriterTaskFromByteSource(hostPath, PathLifeCycles.none(), bs);
    }

    @Override
    public FileWriterTask execToFile(Path hostPath, PathLifeCycle pathLifeCycle) {
        ByteSource bs = toByteSource();
        return new FileWriterTaskFromByteSource(hostPath, PathLifeCycles.none(), bs);
    }

    @Override
    public FileWriterTask runToHostPipe() {
        ByteSource bs = toByteSource();
        PathLifeCycle pathLifeCycle = PathLifeCycles.deleteAfterExec(PathLifeCycles.namedPipe());
        Path tempFile = FileMapper.allocateTempPath("", "");
        return new FileWriterTaskFromByteSource(tempFile, pathLifeCycle, bs);
    }
}
