package org.aksw.shellgebra.registry.codec;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.aksw.commons.io.util.stream.InputStreamTransform;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamProvider;
import org.apache.commons.io.IOUtils;

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
            if (false) {
                try {
                    System.err.println("DATA IN STREAM: " + IOUtils.toString(t, StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return provider.createCompressorInputStream(name, t, true);
        } catch (CompressorException e) {
            throw new RuntimeException("Failed to create compressor input stream for format: " + name, e);
        }
    }

    @Override
    public String toString() {
        return "(" + this.getClass().getSimpleName() + " " + name + ")";
    }
}
