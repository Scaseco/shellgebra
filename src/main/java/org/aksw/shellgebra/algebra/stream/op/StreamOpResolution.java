package org.aksw.shellgebra.algebra.stream.op;

public class StreamOpResolution
    extends StreamOp1
{
    protected Resolution1 resolution;

    public StreamOpResolution(Resolution1 resolution, StreamOp subOp) {
        super(subOp);
        this.resolution = resolution;
    }

    public Resolution1 getResolution() {
        return resolution;
    }

    @Override
    public <T> T accept(StreamOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    @Override
    public String toString() {
        return "StreamOpResolution [resolution=" + resolution + ", getSubOp()=" + getSubOp() + "]";
    }
}
