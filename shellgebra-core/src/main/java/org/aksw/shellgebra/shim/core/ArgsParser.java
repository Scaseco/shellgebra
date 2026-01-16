package org.aksw.shellgebra.shim.core;

public interface ArgsParser<T> {
    T parse(String[] args);
}
