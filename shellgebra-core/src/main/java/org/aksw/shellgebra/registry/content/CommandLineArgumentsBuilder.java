package org.aksw.shellgebra.registry.content;

import java.util.List;

/** Convert parameters of OpStreamContentConvert to arguments for a command invocation.*/
public interface CommandLineArgumentsBuilder {
    List<String> build();
}
