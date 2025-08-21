package org.aksw.shellgebra.registry.codec;

import java.io.InputStream;

import org.aksw.shellgebra.unused.algebra.plan.InputStreamTransform;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamProvider;

public class InputStreamTransformOverCommonsCompress
    implements InputStreamTransform
{
    protected CompressorStreamProvider provider;
    protected String name;

    public InputStreamTransformOverCommonsCompress(CompressorStreamProvider provider, String name) {
        super();
        this.provider = provider;
        this.name = name;
    }

    @Override
    public InputStream apply(InputStream t) {
        try {
            return provider.createCompressorInputStream(name, t, true);
        } catch (CompressorException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "(" + this.getClass().getSimpleName() + " " + name + ")";
    }
}
