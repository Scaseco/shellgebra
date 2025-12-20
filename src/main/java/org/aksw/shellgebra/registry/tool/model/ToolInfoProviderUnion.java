package org.aksw.shellgebra.registry.tool.model;

//public class ToolInfoProviderUnion
//    implements ToolInfoProvider
//{
//    private Collection<ToolInfoProvider> providers;
//
//    protected Stream<ToolInfoProvider> streamProviders() {
//        return providers.stream();
//    }
//
//    @Override
//    public Optional<ToolInfo> get(String toolName) {
//        List<ToolInfo> matches = streamProviders()
//            .map(provider -> provider.get(toolName))
//            .flatMap(Optional::stream)
//            .toList();
//        return toUnion(matches, ms -> new ToolInfoUnion(toolName, matches));
//    }
//
//    public static <T> Optional<T> toUnion(List<T> matches, Function<List<T>, T> combiner) {
//        T tmp = matches.isEmpty()
//            ? null
//            : matches.size() == 1
//                ? matches.iterator().next()
//                : combiner.apply(matches);
//        return Optional.ofNullable(tmp);
//    }
//}
