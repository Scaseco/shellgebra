package org.aksw.shellgebra.exec.graph;

import org.aksw.shellgebra.exec.graph.JRedirect.PRedirectFileDescription;
import org.aksw.shellgebra.exec.graph.JRedirect.PRedirectIn;
import org.aksw.shellgebra.exec.graph.JRedirect.PRedirectJava;
import org.aksw.shellgebra.exec.graph.JRedirect.PRedirectOut;
import org.aksw.shellgebra.exec.graph.JRedirect.PRedirectPBF;

public interface JRedirectVisitor<T> {
    T visit(PRedirectJava redirect);
    T visit(PRedirectFileDescription redirect);
    T visit(PRedirectIn redirect);
    T visit(PRedirectOut redirect);
    T visit(PRedirectPBF redirect);
}
