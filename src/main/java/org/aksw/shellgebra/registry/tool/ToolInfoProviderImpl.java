package org.aksw.shellgebra.registry.tool;

/**
 *
 * TODO Add sanity check whether different tools share the same command (shouldn't happen).
 */
//public class ToolInfoProviderImpl
//    implements ToolInfoProvider
//{
//    protected Map<String, ToolInfoImpl> nameToToolInfo = new LinkedHashMap<>();
//
//    private ToolInfoProviderImpl(Map<String, ToolInfoImpl> nameToToolInfo) {
//        super();
//        this.nameToToolInfo = nameToToolInfo;
//    }
//
//    public ToolInfoProviderImpl() {
//    }
//
//    public Collection<ToolInfoImpl> list() {
//        return nameToToolInfo.values();
//    }
//
//    public ToolInfoImpl getOrCreate(String toolName) {
//        return nameToToolInfo.computeIfAbsent(toolName, tn -> new ToolInfoImpl(toolName));
//    }
//
//    public ToolInfoImpl merge(ToolInfoImpl toolInfo) {
//        ToolInfoImpl result = getOrCreate(toolInfo.getName());
//        for (CommandTargetInfo cpi : toolInfo.list().toList()) {
//            CommandTargetInfo thisCpi = result.getOrCreateCommand(cpi.getCommand());
//            cpi.getDockerImages().forEach(thisCpi::addAvailabilityDockerImage);
//        }
//        result.getAbsenceInDockerImages().addAll(toolInfo.getAbsenceInDockerImages());
//
//        return result;
//    }
//
//    @Override
//    public Optional<ToolInfo> get(String toolName) {
//        return Optional.ofNullable(nameToToolInfo.get(toolName));
//    }
//
//    public static class Builder {
//        protected Map<String, ToolInfoImpl> nameToToolInfo = new LinkedHashMap<>();
//
//        public Builder add(ToolInfoImpl toolInfo) {
//            nameToToolInfo.put(toolInfo.getName(), toolInfo);
//            return this;
//        }
//
//        public ToolInfoProvider build() {
//            return new ToolInfoProviderImpl(Collections.unmodifiableMap(new LinkedHashMap<>(nameToToolInfo)));
//        }
//    }
//
//    public static Builder newBuilder() {
//        return new Builder();
//    }
//
//    @Override
//    public String toString() {
//        return nameToToolInfo.values().toString();
//    }
//}
