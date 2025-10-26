package org.aksw.vshell.registry;

/** Map virtual commands to physical locations. */
// XXX Future resolution of versions might need to go here.
//public class CommandRegistryImpl implements CommandRegistry {
//    private Map<String, Set<CommandLocation>> map = new HashMap<>();
//
//    @Override
//    public Set<CommandLocation> get(String virtualCommandName) {
//        return Optional.ofNullable(map.get(virtualCommandName))
//            .map(Collections::unmodifiableSet)
//            .orElse(Set.of());
//    }
//
//    public CommandRegistryImpl put(String virtualCommandName, CommandLocation location) {
//        map.computeIfAbsent(virtualCommandName, k -> new LinkedHashSet<>())
//            .add(location);
//        return this;
//    }
//}
