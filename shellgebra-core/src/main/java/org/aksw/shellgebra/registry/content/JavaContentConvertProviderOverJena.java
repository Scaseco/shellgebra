package org.aksw.shellgebra.registry.content;

import java.util.Optional;

import org.aksw.commons.io.util.stream.InputStreamTransform;
import org.aksw.shellgebra.algebra.common.OpSpecContentConvertRdf;
import org.aksw.shellgebra.algebra.stream.transform.StreamingRDFConverter;
import org.aksw.shellgebra.registry.codec.JavaStreamTransform;

public class JavaContentConvertProviderOverJena
    implements JavaContentConvertProvider
{
     @Override
    // public Optional<JavaStreamTransform> getConverter(String srcLang, String tgtFormat, String base) {
    public Optional<JavaStreamTransform> getConverter(OpSpecContentConvertRdf spec) {
        InputStreamTransform inXform = StreamingRDFConverter.converter(spec);
        JavaStreamTransform tmp = null;
        if (inXform != null) {
            tmp = new JavaStreamTransform(inXform, null);
        }
        return Optional.ofNullable(tmp);
    }
}
