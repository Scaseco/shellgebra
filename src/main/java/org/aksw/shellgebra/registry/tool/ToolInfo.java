package org.aksw.shellgebra.registry.tool;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class ToolInfo {
    protected String name;
    protected Map<String, CommandPathInfo> commandMap;

    // XXX absentOnHost can be true even though there may be a command - revise design to avoid inconsistency.
    protected Boolean absentOnHost = null;
    protected Set<String> absenceInDockerImages = new LinkedHashSet<>();

    public ToolInfo(String name) {
        this(name, new LinkedHashMap<>(), new LinkedHashSet<>());
    }

    private ToolInfo(String name, Map<String, CommandPathInfo> commandMap, Set<String> absenceInDockerImages) {
        super();
        this.name = name;
        this.commandMap = commandMap;
        this.absenceInDockerImages = absenceInDockerImages;
    }

    public String getName() {
        return name;
    }

    public Set<String> getAbsenceInDockerImages() {
        return absenceInDockerImages;
    }

    public boolean isAbsentInDockerImage(String dockerImage) {
        return absenceInDockerImages.contains(dockerImage);
    }

    public void setAbsentOnHost(Boolean value) {
        absentOnHost = value;
    }

    public Boolean getAbsentOnHost() {
        return absentOnHost;
    }

    public ToolInfo setAbsentInDockerImage(String dockerImage) {
        this.absenceInDockerImages.add(dockerImage);
        return this;
    }

    public CommandPathInfo findCommandByImage(String imageName) {
        CommandPathInfo result = commandMap.values().stream()
            .filter(cpi -> cpi.getDockerImages().contains(imageName))
            .findFirst().orElse(null);
        return result;
    }

    public CommandPathInfo findCommandOnHost() {
        CommandPathInfo result = commandMap.values().stream()
            .filter(cpi -> Boolean.TRUE.equals(cpi.getAvailableOnHost()))
            .findFirst().orElse(null);
        return result;
    }

    public Map<String, CommandPathInfo> getCommandsByPath() {
        return commandMap;
    }

    public Collection<CommandPathInfo> list() {
        return commandMap.values();
    }

    public CommandPathInfo getOrCreateCommand(String commandPath) {
        return commandMap.computeIfAbsent(commandPath, n -> new CommandPathInfo(commandPath));
    }

    @Override
    public ToolInfo clone() {
        Map<String, CommandPathInfo> newCmdMap = commandMap.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().clone(), (u, v) -> u, LinkedHashMap::new));
        return new ToolInfo(name, newCmdMap, new LinkedHashSet<>(absenceInDockerImages));
    }

    @Override
    public String toString() {
        return "ToolInfo [name=" + name + ", commands=" + commandMap.values() + "absentOnHost=" + absentOnHost +  ", absences: " + absenceInDockerImages + "]";
    }

//    public static class Builder {
//        protected String name;
//        protected Map<String, CommandPathInfo> commandMap = new LinkedHashMap<>();
//
//        public Builder setName(String name) {
//            this.name = name;
//            return this;
//        }
//
//        public Builder addCommand(CommandPathInfo commandPath) {
//            commandMap.put(commandPath.getCommand(), commandPath);
//            return this;
//        }
//
//        public Builder addCommands(Collection<CommandPathInfo> commandPaths) {
//            commandPaths.forEach(this::addCommand);
//            return this;
//        }
//
//        public ToolInfo build() {
//            Objects.requireNonNull(name);
//            return new ToolInfo(name, Collections.unmodifiableMap(new LinkedHashMap<>(commandMap)));
//        }
//    }
//
//    public static Builder newBuilder() {
//        return new Builder();
//    }
//
//    public static Builder newBuilder(ToolInfo toolInfo) {
//        return new Builder()
//            .setName(toolInfo.getName())
//            .addCommands(toolInfo.getCommandsByPath().values());
//    }
}
