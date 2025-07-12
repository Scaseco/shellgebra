package org.aksw.shellgebra.registry.codec;

import java.util.Optional;

public interface JavaCodecProvider {
    Optional<JavaCodec> getCodec(String name);
}
