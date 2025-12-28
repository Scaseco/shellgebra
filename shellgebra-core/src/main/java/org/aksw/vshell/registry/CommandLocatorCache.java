package org.aksw.vshell.registry;

import java.util.Optional;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Deprecated // Not needed - caching needs to be done on the command resolver level!
public class CommandLocatorCache
    extends CommandLocatorWrapper
{
    private Cache<String, Optional<String>> cache;

    public CommandLocatorCache(CommandLocator delegate) {
        this(delegate, Long.MAX_VALUE);
    }

    public CommandLocatorCache(CommandLocator delegate, long maxCacheSize) {
        super(delegate);
        this.cache = Caffeine.newBuilder().maximumSize(maxCacheSize).build();
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public Optional<String> locate(String command) {
        return cache.get(command, super::locate);
    }
}
