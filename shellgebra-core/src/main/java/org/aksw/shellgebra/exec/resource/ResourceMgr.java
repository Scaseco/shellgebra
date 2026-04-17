package org.aksw.shellgebra.exec.resource;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.Scheduler;

import org.aksw.commons.util.ref.Ref;
import org.aksw.commons.util.ref.RefImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource manager that loads resources on demand and defers closing.
 * Requesting a resource scheduled for close cancels the schedule.
 *
 * @implNote
 *   Uses Caffeine Cache with a specialized configuration to achieve the "physical-close-only-after-delay" behavior.
 *
 * @param <K>
 * @param <V>
 */
// AI disclaimer: The core of this class (Caffeine setup + ResourceHolder) were largely generated with Gemini on 2026-02-04.
public class ResourceMgr<K, V>
    implements AutoCloseable
{
    private static final Logger logger = LoggerFactory.getLogger(ResourceMgr.class);
    private Function<K, V> loader;
    private Consumer<V> closer;
    private Duration evictionDelay;

    private final Cache<K, ResourceEntry<V>> cache;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    private ResourceMgr(Function<K, V> loader, Consumer<V> closer, Duration evictionDelay) {
        super();
        this.loader = loader;
        this.closer = closer;
        this.evictionDelay = evictionDelay;

        long nanos = evictionDelay.toNanos();

        this.cache = Caffeine.newBuilder()
            .scheduler(Scheduler.systemScheduler())
            .expireAfter(new Expiry<K, ResourceEntry<V>>() {
                @Override
                public long expireAfterCreate(K k, ResourceEntry<V> h, long t) {
                    return h.state.get() > 0 ? Long.MAX_VALUE : nanos;
                }
                @Override
                public long expireAfterUpdate(K k, ResourceEntry<V> h, long t, long d) {
                    return h.state.get() > 0 ? Long.MAX_VALUE : nanos;
                }
                @Override
                public long expireAfterRead(K k, ResourceEntry<V> h, long t, long d) { return d; }
            })
            .removalListener((k, holder, cause) -> {
                closeEntry(k, holder);
            })
            .build();
    }

    public static <K, V> ResourceMgr<K, V> of(Function<K, V> loader, Duration evictionDelay) {
        return new ResourceMgr<>(loader, v -> {}, evictionDelay);
    }

    public static <K, V> ResourceMgr<K, V> of(Function<K, V> loader, Consumer<V> closer, Duration evictionDelay) {
        return new ResourceMgr<>(loader, closer, evictionDelay);
    }

    public Ref<V> get(K key) {
        if (isClosed.get()) {
            throw new IllegalStateException("Manager is closed");
        }

        while (true) {
            ResourceEntry<V> holder = cache.get(key, k -> new ResourceEntry<>(loader.apply(k), 0));
            // The holder might be a dead existing one.
            // We must ensure the cache knows it's "active" again.
            if (holder.tryIncrement()) {
                cache.put(key, holder);
                // If the manager was closed concurrently, then self-destruct immediately.
                if (isClosed.get()) {
                    if (holder.tryDecrement()) {
                        release(key, holder);
                    }
                    throw new IllegalStateException("Manager was closed during resource acquisition");
                }
                // return new Ref<>(key, holder, this);
                return RefImpl.create(holder.resource, new Object(), () -> {
                    release(key, holder);
                });
            }
            // If tryIncrement failed, it means the holder was marked -1 (closed)
            // by the removal thread. We must invalidate and loop to create a new one.
            cache.invalidate(key);
        }
    }

    @SuppressWarnings("unused") // Key is used for {cache.asMap().forEach(this::closeEntry)}
    private void closeEntry(K key, ResourceEntry<V> holder) {
        // Only close if we successfully transition from 0 to -1
        if (holder != null && holder.markClosed()) {
            try {
                // logger.debug("Actual close called.");
                closer.accept(holder.resource);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void release(K key, ResourceEntry<V> holder) {
        if (holder.tryDecrement()) {
            if (holder.state.get() == 0) {
                cache.put(key, holder); // Trigger the deferred close window
            }
        }
    }

    private void invalidateAll() {
        cache.asMap().forEach(this::closeEntry);
        cache.invalidateAll();
        cache.cleanUp();
    }

    @Override
    public void close() {
        if (isClosed.compareAndSet(false, true)) {
            invalidateAll();
        }
    }
}

class ResourceEntry<V> {
    final V resource;
    // Positive = active refs, 0 = in grace period, -1 = closed/dead
    final AtomicInteger state;

    public ResourceEntry(V resource, int initialRefs) {
        this.resource = resource;
        this.state = new AtomicInteger(initialRefs);
    }

    public boolean tryIncrement() {
        while (true) {
            int current = state.get();
            if (current < 0) return false; // Already closed, cannot revive
            if (state.compareAndSet(current, current + 1)) return true;
        }
    }

    public boolean tryDecrement() {
        while (true) {
            int current = state.get();
            if (current <= 0) return false;
            if (state.compareAndSet(current, current - 1)) return true;
        }
    }

    public boolean markClosed() {
        // Transition from 0 (grace) to -1 (dead)
        return state.compareAndSet(0, -1);
    }

    @Override
    public String toString() {
        return "ResourceHolder [state=" + state + ", resource=" + resource + "]";
    }
}

/*
public static void main(String...args) throws InterruptedException {
    ResourceMgr<String, String> mgr = ResourceMgr.of(
        k -> "yay:" + System.currentTimeMillis(),
        Duration.ofSeconds(5));

    {
        try (Ref<String> ref = mgr.get("hi")) {
            logger.debug("{}", ref.get());
        }
    }

    {
        try (Ref<String> ref = mgr.get("hi")) {
            logger.debug("{}", ref.get());
        }
    }

    Thread.sleep(10000);

    {
        try (Ref<String> ref = mgr.get("hi")) {
            logger.debug("{}", ref.get());
        }
    }
}
*/
