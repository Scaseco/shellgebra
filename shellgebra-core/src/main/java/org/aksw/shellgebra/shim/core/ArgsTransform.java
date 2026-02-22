package org.aksw.shellgebra.shim.core;

import java.util.List;

public interface ArgsTransform {
    List<String> map(List<String> args);

    /** Return a transform that returns the input arguments. */
    static ArgsTransform identity() {
        return ArgsTransformInternals.identity;
    }

    /** Return a transform that expects no arguments and returns a static list of arguments as the transformation result. */
    static ArgsTransform noArgs(String... args) {
        List<String> staticArgs = List.of(args);
        return in -> {
            if (!in.isEmpty()) {
                throw new IllegalArgumentException("No arguments expected.");
            }
            return staticArgs;
        };
    }
}

class ArgsTransformInternals {
    static final ArgsTransform identity = x -> x;
}
