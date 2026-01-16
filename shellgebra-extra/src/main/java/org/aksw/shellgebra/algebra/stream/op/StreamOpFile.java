package org.aksw.shellgebra.algebra.stream.op;

import java.util.Objects;

import org.aksw.shellgebra.algebra.common.OpSpecFile;

public class StreamOpFile
    extends StreamOp0
{
    // protected String path;
    protected OpSpecFile opSpec;

    public StreamOpFile(String path) {
        this(new OpSpecFile(Objects.requireNonNull(path)));
    }

    public StreamOpFile(OpSpecFile opSpec) {
        super();
        this.opSpec = Objects.requireNonNull(opSpec);
    }

    public OpSpecFile opSpec() {
        return opSpec;
    }

    public String getPath() {
        return opSpec.name();
    }

    @Override
    public <T> T accept(StreamOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    @Override
    public String toString() {
        return "(file (" + getPath() + "))";
    }
}
