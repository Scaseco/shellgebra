package org.aksw.commons.util.docker;

import org.aksw.jenax.model.osreo.ImageIntrospection;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

public class ImageIntrospectorCaching
    extends ImageIntrospectorWrapper
{
    private Cache<String, Result<ImageIntrospection>> cache;

    public ImageIntrospectorCaching(ImageIntrospector delegate) {
        this(delegate, Long.MAX_VALUE);
    }

    public ImageIntrospectorCaching(ImageIntrospector delegate, long maxCacheSize) {
        super(delegate);
        this.cache = Caffeine.newBuilder().maximumSize(maxCacheSize).build();
    }

    @Override
    public ImageIntrospection inspect(String image, boolean pullIfAbsent) {
        Result<ImageIntrospection> x = cache.get(image, k -> {
            Result<ImageIntrospection> r;
            try {
                r = new Result.Ok<>(super.inspect(image, pullIfAbsent));
            } catch(Throwable e) {
                r = new Result.Err<>(e);
            }
            return r;
        });
        return x.getOrElseThrow();
    }
}
