package org.aksw.shellgebra.exec.model;

public class ExecSites {
    public static ExecSite host() {
        return ExecSiteCurrentHost.get();
    }

    public static ExecSite jvm() {
        return ExecSiteCurrentJvm.get();
    }

    public static ExecSite docker(String imageRef) {
        return new ExecSiteDockerImage(imageRef);
    }
}
