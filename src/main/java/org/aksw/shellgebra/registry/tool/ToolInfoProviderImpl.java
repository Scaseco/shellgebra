package org.aksw.shellgebra.registry.tool;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class ToolInfoProviderImpl
    implements ToolInfoProvider
{
    protected Map<String, ToolInfo> nameToToolInfo = new LinkedHashMap<>();

    private ToolInfoProviderImpl(Map<String, ToolInfo> nameToToolInfo) {
        super();
        this.nameToToolInfo = nameToToolInfo;
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
}
