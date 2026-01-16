package org.aksw.shellgebra.algebra.cmd.op;

import org.aksw.shellgebra.algebra.cmd.op.RedirectTarget.RedirectTargetFile;
import org.aksw.shellgebra.algebra.cmd.op.RedirectTarget.RedirectTargetProcessSubstitution;

public interface RedirectTargetVisitor<T> {
    T visit(RedirectTargetFile redirect);
    T visit(RedirectTargetProcessSubstitution redirect);
}
