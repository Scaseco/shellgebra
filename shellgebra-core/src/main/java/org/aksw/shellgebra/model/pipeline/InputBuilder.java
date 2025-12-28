package org.aksw.shellgebra.model.pipeline;

import java.io.InputStream;
import java.nio.file.Path;

public interface InputBuilder {
    OutputBuilder from(InputStream in);
    OutputBuilder from(Path path);
    OutputBuilder from(ProcessBuilder processBuilder);
}
