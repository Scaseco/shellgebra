package org.aksw.shellgebra.shim.core;

import java.util.List;

public interface ArgsParser<T> {
    T parse(String[] args);

    default T parse(List<String> args) {
        String[] arr = args.toArray(String[]::new);
        return parse(arr);
    }
}
