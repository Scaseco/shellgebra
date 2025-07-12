package org.aksw.shellgebra.registry.tool;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class ToolInfo {
    protected String name;
    protected Map<String, CommandPathInfo> commandMap;

    private ToolInfo(String name, Map<String, CommandPathInfo> commandMap) {
        super();
        this.name = name;
        this.commandMap = commandMap;
    }

    public String getName() {
        return name;
    }

    public Map<String, CommandPathInfo> getCommandsByPath() {
        return commandMap;
    }

    public static class Builder {
        protected String name;
        protected Map<String, CommandPathInfo> commandMap = new LinkedHashMap<>();

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder addCommand(CommandPathInfo commandPath) {
            commandMap.put(commandPath.getCommand(), commandPath);
            return this;
        }

        public ToolInfo build() {
            Objects.requireNonNull(name);
            return new ToolInfo(name, Collections.unmodifiableMap(new LinkedHashMap<>(commandMap)));
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }
}
