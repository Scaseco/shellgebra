package org.aksw.shellgebra.algebra.cmd.redirect;

public sealed interface Redirect permits RedirectFile {
    <T> T accept(RedirectVisitor<T> visitor);
}

