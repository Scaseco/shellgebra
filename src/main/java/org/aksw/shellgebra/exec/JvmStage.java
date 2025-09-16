package org.aksw.shellgebra.exec;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.aksw.shellgebra.unused.algebra.plan.InputStreamTransform;

import com.google.common.io.ByteSource;

public class JvmStage
    implements Stage
{
    private InputStreamTransform transform;

    public JvmStage(InputStreamTransform transform) {
        super();
        this.transform = Objects.requireNonNull(transform);
    }

    @Override
    public BoundStage from(ByteSource input) {
        return new JvmBoundStage(transform, input);
    }

    @Override
    public BoundStage from(FileWriterTask input) {
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
        return new JvmBoundStage(transform, bs);
    }

    @Override
    public BoundStage from(BoundStage input) {
        return new JvmBoundStage(transform, input.toByteSource());
    }

    @Override
    public BoundStage fromNull() {
        return from(ByteSource.empty());
    }

    @Override
    public String toString() {
        return "(jvmStage " +  transform.toString() + ")";
    }
}
