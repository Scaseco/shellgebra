package org.aksw.shellgebra.registry.codec;

import java.util.Set;

import org.aksw.commons.io.util.stream.InputStreamTransform;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.CompressorStreamProvider;

public class InputStreamTransformProviderOverCommonsCompress
    implements InputStreamTransformProvider
{
    protected CompressorStreamProvider provider;

    public InputStreamTransformProviderOverCommonsCompress() {
        this(new CompressorStreamFactory());
    }

    public InputStreamTransformProviderOverCommonsCompress(CompressorStreamProvider provider) {
        super();
        this.provider = provider;
    }

    @Override
    public InputStreamTransform get(String name) {
        return get(provider, name);
    }

    public static InputStreamTransform get(CompressorStreamProvider provider, String name) {
        Set<String> names = provider.getInputStreamCompressorNames();
        InputStreamTransform result = names.contains(name)
            ? new InputStreamTransformOverCommonsCompress(provider, name)
            : null;
        return result;
    }
}
