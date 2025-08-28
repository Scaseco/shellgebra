package org.aksw.shellgebra.exec;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.exec.FileWriterTaskBase.PathLifeCycle;
import org.aksw.shellgebra.unused.algebra.plan.InputStreamTransform;

import com.google.common.io.ByteSource;

public class ExecFactoryInputStreamTransform
    implements ExecFactory
{
    private InputStreamTransform transform;

    public ExecFactoryInputStreamTransform(InputStreamTransform transform) {
        super();
        this.transform = Objects.requireNonNull(transform);
    }

    public static ExecFactory of(InputStreamTransform transform) {
        return new ExecFactoryInputStreamTransform(transform);
    }

    @Override
    public ExecBuilder forInput(ByteSource input) {
        return new ExecBuilderInputStreamTransform(transform, input);
    }

    @Override
    public ExecBuilder forInput(FileWriterTask input) {
        // TODO Protect against multiple starts of the same input!
        ByteSource bs = new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                Path path = input.getOutputPath();
                input.start();
                InputStream in = Files.newInputStream(path);
                InputStream result = new FilterInputStream(in) {
                    @Override
                    public void close() throws IOException {
                        try {
                            input.close();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        } finally {
                            super.close();
                        }
                    }
                };
                return result;
            }
        };
        return new ExecBuilderInputStreamTransform(transform, bs);
    }

    @Override
    public ExecBuilder forInput(ExecBuilder input) {
        return new ExecBuilderInputStreamTransform(transform, input.toByteSource());
    }

    @Override
    public ExecBuilder forNullInput() {
        return forInput(ByteSource.empty());
    }
}

class ExecBuilderInputStreamTransform
    implements ExecBuilder
{
    private InputStreamTransform transform;
    private ByteSource byteSource;

    public ExecBuilderInputStreamTransform(InputStreamTransform transform, ByteSource byteSource) {
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
