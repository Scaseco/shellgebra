package org.aksw.shellgebra.shim.core;

/** Functional interface to transform a domain type into an argument list. */
public interface ArgumentListRenderer<T> {
    ArgumentList toArgumentList(T model);
}
