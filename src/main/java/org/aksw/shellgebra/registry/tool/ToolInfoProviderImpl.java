package org.aksw.shellgebra.registry.tool;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 * TODO Add sanity check whether different tools share the same command (shouldn't happen).
 */
public class ToolInfoProviderImpl
    implements ToolInfoProvider
{
    protected Map<String, ToolInfo> nameToToolInfo = new LinkedHashMap<>();

    private ToolInfoProviderImpl(Map<String, ToolInfo> nameToToolInfo) {
        super();
        this.nameToToolInfo = nameToToolInfo;
    }

    public ToolInfoProviderImpl() {
    }

    public Collection<ToolInfo> list() {
        return nameToToolInfo.values();
    }

    public ToolInfo getOrCreate(String toolName) {
        return nameToToolInfo.computeIfAbsent(toolName, tn -> new ToolInfo(toolName));
    }

    public ToolInfo merge(ToolInfo toolInfo) {
        ToolInfo result = getOrCreate(toolInfo.getName());
        for (CommandPathInfo cpi :  toolInfo.list()) {
            CommandPathInfo thisCpi = result.getOrCreateCommand(cpi.getCommand());
            cpi.getDockerImages().forEach(thisCpi::addDockerImageAvailability);
        }
        result.getAbsenceInDockerImages().addAll(toolInfo.getAbsenceInDockerImages());

        return result;
    }

    @Override
    public Optional<ToolInfo> get(String toolName) {
        return Optional.ofNullable(nameToToolInfo.get(toolName));
    }

    public static class Builder {
        protected Map<String, ToolInfo> nameToToolInfo = new LinkedHashMap<>();

        public Builder add(ToolInfo toolInfo) {
            nameToToolInfo.put(toolInfo.getName(), toolInfo);
            return this;
        }

        public ToolInfoProvider build() {
            return new ToolInfoProviderImpl(Collections.unmodifiableMap(new LinkedHashMap<>(nameToToolInfo)));
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return nameToToolInfo.values().toString();
    }
}
