package org.aksw.shellgebra.shim.core;

public interface ArgumentListRenderer<T> {
    ArgumentList toArgumentList(T model);
}
