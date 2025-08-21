package org.aksw.shellgebra.exec.model;

import java.util.Objects;

// https://stackoverflow.com/questions/55277250/what-are-the-appropriate-names-for-the-parts-of-a-docker-images-name
public record ExecSiteDockerImage(String imageName)
    implements ExecSite
{
    public ExecSiteDockerImage(String imageName) {
        this.imageName = Objects.requireNonNull(imageName);
    }
}
