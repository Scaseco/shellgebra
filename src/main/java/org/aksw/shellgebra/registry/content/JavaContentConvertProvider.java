package org.aksw.shellgebra.registry.content;

import java.util.Optional;

import org.aksw.shellgebra.algebra.common.OpSpecContentConvertRdf;
import org.aksw.shellgebra.registry.codec.JavaStreamTransform;

public interface JavaContentConvertProvider {
    Optional<JavaStreamTransform> getConverter(OpSpecContentConvertRdf spec);

    // Optional<JavaStreamTransform> getConverter(String srcFormat, String tgtFormat, String base);
}
