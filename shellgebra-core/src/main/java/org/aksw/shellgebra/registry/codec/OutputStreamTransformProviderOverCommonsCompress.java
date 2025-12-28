package org.aksw.shellgebra.registry.codec;

import java.util.Set;

import org.aksw.commons.io.util.stream.OutputStreamTransform;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.CompressorStreamProvider;

public class OutputStreamTransformProviderOverCommonsCompress
    implements OutputStreamTransformProvider
{
    protected CompressorStreamProvider provider;

    public OutputStreamTransformProviderOverCommonsCompress() {
        this(new CompressorStreamFactory());
    }

    public OutputStreamTransformProviderOverCommonsCompress(CompressorStreamProvider provider) {
        super();
        this.provider = provider;
    }

    @Override
    public OutputStreamTransform get(String name) {
        return get(provider, name);
    }

    public static OutputStreamTransform get(CompressorStreamProvider provider, String name) {
        Set<String> names = provider.getOutputStreamCompressorNames();
        OutputStreamTransform result = names.contains(name)
            ? new OutputStreamTransformOverCommonsCompress(provider, name)
            : null;
        return result;
    }
}
