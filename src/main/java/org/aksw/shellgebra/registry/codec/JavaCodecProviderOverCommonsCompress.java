package org.aksw.shellgebra.registry.codec;

import java.util.Optional;

import org.aksw.shellgebra.unused.algebra.plan.InputStreamTransform;
import org.aksw.shellgebra.unused.algebra.plan.OutputStreamTransform;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.CompressorStreamProvider;

public class JavaCodecProviderOverCommonsCompress
    implements JavaCodecProvider
{
    protected CompressorStreamProvider provider;

    public JavaCodecProviderOverCommonsCompress() {
        this(new CompressorStreamFactory());
    }

    public JavaCodecProviderOverCommonsCompress(CompressorStreamProvider provider) {
        super();
        this.provider = provider;
    }

    @Override
    public Optional<JavaCodec> getCodec(String name) {
        InputStreamTransform isXform = InputStreamTransformProviderOverCommonsCompress.get(provider, name);
        OutputStreamTransform osXform = OutputStreamTransformProviderOverCommonsCompress.get(provider, name);

        JavaCodec tmp = null;
        if (isXform != null || osXform != null) {
            JavaStreamTransform decoder = new JavaStreamTransform(isXform, null);
            JavaStreamTransform encoder = new JavaStreamTransform(null, osXform);
            tmp = new JavaCodec(decoder, encoder);
        }

        return Optional.ofNullable(tmp);
    }
}
