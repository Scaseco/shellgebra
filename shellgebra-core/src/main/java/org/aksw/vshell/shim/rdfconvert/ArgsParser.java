package org.aksw.vshell.shim.rdfconvert;

public interface ArgsParser<T> {
    T parse(String[] args);
}
