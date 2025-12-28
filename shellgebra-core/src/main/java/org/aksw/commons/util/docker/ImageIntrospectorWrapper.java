package org.aksw.commons.util.docker;

import org.aksw.jenax.model.osreo.ImageIntrospection;

public class ImageIntrospectorWrapper
    implements ImageIntrospector
{
    private ImageIntrospector delegate;

    public ImageIntrospectorWrapper(ImageIntrospector delegate) {
        super();
        this.delegate = delegate;
    }

    public ImageIntrospector getDelegate() {
        return delegate;
    }

    @Override
    public ImageIntrospection inspect(String image, boolean pullIfAbsent) {
        return getDelegate().inspect(image, pullIfAbsent);
    }
}
