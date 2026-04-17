package org.aksw.shellgebra.shim.cmd;

import java.io.InputStream;

import org.aksw.commons.io.util.stream.InputStreamTransform;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputStreamTransformOverCommonsCompress
    implements InputStreamTransform
{
    protected CompressorStreamProvider provider;
    protected String name;

    private static final Logger logger = LoggerFactory.getLogger(InputStreamTransformOverCommonsCompress.class);

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
            throw new RuntimeException("Failed to create compressor input stream for format: " + name, e);
        }
    }

    @Override
    public String toString() {
        return "(" + this.getClass().getSimpleName() + " " + name + ")";
    }
}
