package org.aksw.shellgebra.shim.core;

import java.util.List;

public interface ArgsTransform {
    List<String> map(List<String> args);

    static ArgsTransform identity() {
        return ArgsTransformInternals.identity;
    }
}

class ArgsTransformInternals {
    static final ArgsTransform identity = x -> x;
}
