package org.aksw.shellgebra.registry.codec;

import java.io.OutputStream;

import org.aksw.commons.io.util.stream.OutputStreamTransform;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamProvider;

public class OutputStreamTransformOverCommonsCompress
    implements OutputStreamTransform
{
    protected CompressorStreamProvider provider;
    protected String name;

    public OutputStreamTransformOverCommonsCompress(CompressorStreamProvider provider, String name) {
        super();
        this.provider = provider;
        this.name = name;
    }

    @Override
    public OutputStream apply(OutputStream t) {
        try {
            return provider.createCompressorOutputStream(name, t);
        } catch (CompressorException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "(" + this.getClass().getSimpleName() + " " + name + ")";
    }
}
