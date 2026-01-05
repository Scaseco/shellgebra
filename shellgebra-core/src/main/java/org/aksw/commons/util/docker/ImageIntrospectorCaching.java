package org.aksw.commons.util.docker;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import org.aksw.shellgebra.model.osreo.ImageIntrospection;
import org.aksw.shellgebra.model.osreo.ImageIntrospector;

//public class ImageIntrospectorCaching
//    extends ImageIntrospectorWrapper
//{
//    private Cache<String, Result<ImageIntrospection>> cache;
//
//    public ImageIntrospectorCaching(ImageIntrospector delegate) {
//        this(delegate, Long.MAX_VALUE);
//    }
//
//    public ImageIntrospectorCaching(ImageIntrospector delegate, long maxCacheSize) {
//        super(delegate);
//        this.cache = Caffeine.newBuilder().maximumSize(maxCacheSize).build();
//    }
//
//    @Override
//    public ImageIntrospection findShell(String image, boolean pullIfAbsent) {
//        Result<ImageIntrospection> x = cache.get(image, k -> {
//            Result<ImageIntrospection> r;
//            try {
//                r = new Result.Ok<>(super.findShell(image, pullIfAbsent));
//            } catch(Throwable e) {
//                r = new Result.Err<>(e);
//            }
//            return r;
//        });
//        return x.getOrElseThrow();
//    }
//}
