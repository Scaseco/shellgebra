package org.aksw.shellgebra.exec.model;

public class ExecSiteDocker
    implements ExecSite
{
    private String imageName;

    public ExecSiteDocker(String imageName) {
        super();
        this.imageName = imageName;
    }

    public String getImageName() {
        return imageName;
    }
}
