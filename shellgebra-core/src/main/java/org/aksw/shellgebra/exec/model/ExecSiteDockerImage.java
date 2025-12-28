package org.aksw.shellgebra.exec.model;

import java.util.Objects;

// https://stackoverflow.com/questions/55277250/what-are-the-appropriate-names-for-the-parts-of-a-docker-images-name
public record ExecSiteDockerImage(String imageRef)
    implements ExecSite
{
    public ExecSiteDockerImage(String imageRef) {
        this.imageRef = Objects.requireNonNull(imageRef);
    }

    @Override
    public <T> T accept(ExecSiteVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
