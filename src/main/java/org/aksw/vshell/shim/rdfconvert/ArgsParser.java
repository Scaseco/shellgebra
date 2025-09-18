package org.aksw.vshell.shim.rdfconvert;

public interface ArgsParser<T extends Args> {
    T parse(String[] args);
}
