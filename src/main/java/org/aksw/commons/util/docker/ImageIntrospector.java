package org.aksw.commons.util.docker;

import org.aksw.jenax.model.osreo.ImageIntrospection;

/** Introspects images by launching containers. */
public interface ImageIntrospector {
    ImageIntrospection inspect(String image, boolean pullIfAbsent);
}
