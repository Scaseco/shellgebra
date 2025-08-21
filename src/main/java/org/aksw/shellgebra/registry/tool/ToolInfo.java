package org.aksw.shellgebra.registry.tool;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

// XXX Could separate host from image commands - but probably this would just increase complexity.
public class ToolInfo {
    protected String name;
    protected Map<String, CommandTargetInfo> commandMap;

    // XXX absentOnHost can be true even though there may be a command - revise design to avoid inconsistency.
    protected Boolean absentOnHost = null;

    // Known absences in images.
    protected Set<String> absenceInDockerImages = new LinkedHashSet<>();

    public ToolInfo(String name) {
        this(name, new LinkedHashMap<>(), new LinkedHashSet<>());
    }

    private ToolInfo(String name, Map<String, CommandTargetInfo> commandMap, Set<String> absenceInDockerImages) {
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

    public CommandTargetInfo findCommandByImage(String imageName) {
        CommandTargetInfo result = commandMap.values().stream()
            .filter(cpi -> cpi.getDockerImages().contains(imageName))
            .findFirst().orElse(null);
        return result;
    }

    public CommandTargetInfo findCommandOnHost() {
        CommandTargetInfo result = commandMap.values().stream()
            .filter(cpi -> Boolean.TRUE.equals(cpi.getAvailableOnHost()))
            .findFirst().orElse(null);
        return result;
    }

    public Map<String, CommandTargetInfo> getCommandsByPath() {
        return commandMap;
    }

    public Collection<CommandTargetInfo> list() {
        return commandMap.values();
    }

    public CommandTargetInfo getOrCreateCommand(String commandPath) {
        return commandMap.computeIfAbsent(commandPath, n -> new CommandTargetInfo(commandPath));
    }

    @Override
    public ToolInfo clone() {
        Map<String, CommandTargetInfo> newCmdMap = commandMap.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().clone(), (u, v) -> u, LinkedHashMap::new));
        return new ToolInfo(name, newCmdMap, new LinkedHashSet<>(absenceInDockerImages));
    }

    @Override
    public String toString() {
        return "ToolInfo [name=" + name + ", commandTargets=" + commandMap.values() + ", absentOnHost=" + absentOnHost +  ", absences: " + absenceInDockerImages + "]";
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
