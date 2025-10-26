package org.aksw.shellgebra.registry.content;

import java.util.Optional;

import org.aksw.shellgebra.algebra.common.OpSpecContentConvertRdf;

public interface ContentConvertProvider {
    // Optional<Tool> getConverter(String srcFormat, String tgtFormat, String base);
    Optional<Tool> getConverter(OpSpecContentConvertRdf spec);
}
