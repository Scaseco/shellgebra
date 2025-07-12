package org.aksw.shellgebra.registry.content;

import java.util.Optional;

import org.aksw.shellgebra.registry.codec.JavaStreamTransform;

public interface JavaContentConvertProvider {
    Optional<JavaStreamTransform> getConverter(String srcFormat, String tgtFormat, String base);
}
