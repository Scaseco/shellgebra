package org.aksw.shellgebra.exec.model;

public interface ExecSiteVisitor<T> {
    T visit(ExecSiteDockerImage execSite);
    T visit(ExecSiteCurrentHost execSite);
    T visit(ExecSiteCurrentJvm execSite);
}
