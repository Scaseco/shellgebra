package org.aksw.shellgebra.exec.virtual;

import org.aksw.shellgebra.exec.BoundStage;
import org.aksw.shellgebra.exec.FileWriterTask;

import com.google.common.io.ByteSource;

public interface InputHolder {
    public enum Type {
        BYTE_SOURCE,
        FILE_WRITER_TASK,
        BOUND_STAGE
    }

    Type getType();

    public record ByteSourceInput(ByteSource byteSource) implements InputHolder {
        //public ByteSourceInput { Objects.requireNonNull(byteSource); }
        @Override public Type getType() { return Type.BYTE_SOURCE; }
    }

    public record FileWriterTaskInput(FileWriterTask fileWriterTask) implements InputHolder {
        //public FileWriterTaskInput { Objects.requireNonNull(fileWriterTask); }
        @Override public Type getType() { return Type.FILE_WRITER_TASK; }
    }

    public record BoundStageInput(BoundStage boundStage) implements InputHolder {
        // public BoundStageInput { Objects.requireNonNull(boundStage); }
        @Override public Type getType() { return Type.BOUND_STAGE; }
    }
}

