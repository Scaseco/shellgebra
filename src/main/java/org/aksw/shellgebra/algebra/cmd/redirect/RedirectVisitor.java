package org.aksw.shellgebra.algebra.cmd.redirect;

public interface RedirectVisitor<T> {
    T visit(RedirectFile redirect);
}
