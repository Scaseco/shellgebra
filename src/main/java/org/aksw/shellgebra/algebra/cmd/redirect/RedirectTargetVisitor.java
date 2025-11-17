package org.aksw.shellgebra.algebra.cmd.redirect;

import org.aksw.shellgebra.algebra.cmd.redirect.RedirectTarget.RedirectTargetFile;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectTarget.RedirectTargetProcessSubstitution;

public interface RedirectTargetVisitor<T> {
    T visit(RedirectTargetFile redirect);
    T visit(RedirectTargetProcessSubstitution redirect);
}
