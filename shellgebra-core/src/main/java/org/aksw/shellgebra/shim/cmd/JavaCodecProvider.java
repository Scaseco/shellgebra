package org.aksw.shellgebra.shim.cmd;

import java.util.Optional;

public interface JavaCodecProvider {
    Optional<JavaCodec> getCodec(String name);
}
