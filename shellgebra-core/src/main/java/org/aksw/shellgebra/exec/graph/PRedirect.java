package org.aksw.shellgebra.exec.graph;

import java.lang.ProcessBuilder.Redirect;

// High level redirect
public sealed interface PRedirect {
    // Standard Java redirect
    public record PRedirectJava(Redirect redirect) implements PRedirect { }
//    public record PRedirectProcess(PBF processBuilderFactory) implements PRedirect { }
}
