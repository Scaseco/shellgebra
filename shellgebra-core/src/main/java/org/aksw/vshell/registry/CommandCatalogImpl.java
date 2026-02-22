package org.aksw.vshell.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CommandCatalogImpl
    implements CommandCatalog
{
    private Map<String, List<String>> commandToLocation = new ConcurrentHashMap<>();

    // @Override
    public CommandCatalogImpl put(String commandName, String locationCandidate) {
        commandToLocation.computeIfAbsent(commandName, k -> Collections.synchronizedList(new ArrayList<>()))
            .add(locationCandidate);
        return this;
    }

    @Override
    public Optional<Collection<String>> get(String commandName) {
        Collection<String> tmp = commandToLocation.get(commandName);
        return Optional.ofNullable(tmp);
    }
}
