package org.aksw.shellgebra.algebra.stream.op;

import java.util.Objects;

import org.aksw.shellgebra.algebra.common.OpSpecContentConvertRdf;

/** Byte-level operation. Transform a stream of bytes into another one with a content type conversion applied. */
public class StreamOpContentConvert
    extends StreamOp1
{
    protected OpSpecContentConvertRdf contentConvertSpec;

    public StreamOpContentConvert(String sourceFormat, String targetFormat, String baseIri, StreamOp subOp) {
        this(new OpSpecContentConvertRdf(sourceFormat, targetFormat, baseIri), subOp);
    }

    public StreamOpContentConvert(OpSpecContentConvertRdf contentConvertspec, StreamOp subOp) {
        super(subOp);
        this.contentConvertSpec = Objects.requireNonNull(contentConvertspec);
    }

    public OpSpecContentConvertRdf getContentConvertSpec() {
        return contentConvertSpec;
    }

    public String getSourceFormat() {
        return contentConvertSpec.sourceFormat();
    }

    public String getTargetFormat() {
        return contentConvertSpec.targetFormat();
    }

    public String getBaseIri() {
        return contentConvertSpec.baseIri();
    }

    @Override
    public <T> T accept(StreamOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    @Override
    public String toString() {
        return "(convert (" + subOp + " " + getSourceFormat() + " " + getTargetFormat() + " " + getBaseIri() + "))";
    }
}
