package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;

import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.exec.FileWriterTaskBase.PathLifeCycle;
import org.aksw.shellgebra.unused.algebra.plan.InputStreamTransform;

import com.google.common.io.ByteSource;

public class JvmBoundStage
    implements BoundStage
{
    private InputStreamTransform transform;
    private ByteSource byteSource;

    public JvmBoundStage(InputStreamTransform transform, ByteSource byteSource) {
        super();
        this.transform = Objects.requireNonNull(transform);
        this.byteSource = Objects.requireNonNull(byteSource);
    }

    @Override
    public ByteSource toByteSource() {
        return new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                InputStream in = byteSource.openStream();
                InputStream result = transform.apply(in);
                return result;
            }
        };
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