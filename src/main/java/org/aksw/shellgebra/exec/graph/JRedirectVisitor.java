package org.aksw.shellgebra.exec.graph;

import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectFileDescription;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectIn;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectJava;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectOut;

public interface JRedirectVisitor<T> {
    T visit(JRedirectJava redirect);
    T visit(JRedirectFileDescription redirect);
    T visit(JRedirectIn redirect);
    T visit(JRedirectOut redirect);
//    T visit(JRedirectPBF redirect);

    // An anonymous pipe has a file name - so perhaps this should be detected by analyzing the file name.
    // T visit(JRedirectAnonymousPipe redirect);
}
