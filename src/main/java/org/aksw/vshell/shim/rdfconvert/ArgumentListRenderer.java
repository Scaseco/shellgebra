package org.aksw.vshell.shim.rdfconvert;

public interface ArgumentListRenderer<T> {
    ArgumentList toArgumentList(T model);
}
