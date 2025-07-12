package org.aksw.shellgebra.registry.content;

import java.util.Optional;

public interface ContentConvertProvider {
    Optional<Tool> getConverter(String srcFormat, String tgtFormat, String base);
}
