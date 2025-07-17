package org.aksw.shellgebra.registry.content;

import java.util.Optional;

import org.aksw.shellgebra.algebra.stream.op.ContentConvertSpec;
import org.aksw.shellgebra.algebra.stream.transform.StreamingRDFConverter;
import org.aksw.shellgebra.registry.codec.JavaStreamTransform;
import org.aksw.shellgebra.unused.algebra.plan.InputStreamTransform;

public class JavaContentConvertProviderOverJena
    implements JavaContentConvertProvider
{
     @Override
    // public Optional<JavaStreamTransform> getConverter(String srcLang, String tgtFormat, String base) {
    public Optional<JavaStreamTransform> getConverter(ContentConvertSpec spec) {
        InputStreamTransform inXform = StreamingRDFConverter.converter(spec);
        JavaStreamTransform tmp = null;
        if (inXform != null) {
            tmp = new JavaStreamTransform(inXform, null);
        }
        return Optional.ofNullable(tmp);
    }
}
