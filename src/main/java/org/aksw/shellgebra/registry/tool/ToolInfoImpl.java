package org.aksw.shellgebra.registry.tool;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.shellgebra.registry.tool.model.CommandTargetInfo;
import org.aksw.shellgebra.registry.tool.model.ToolInfo;

// XXX Could separate host from image commands - but probably this would just increase complexity.
public class ToolInfoImpl
    implements ToolInfo
{
    protected String name;
    protected Map<String, CommandTargetInfoImpl> commandMap;

    // XXX absentOnHost can be true even though there may be a command - revise design to avoid inconsistency.
    protected Optional<Boolean> absentOnHost = null;

    // Known absences in images.
    protected Set<String> absenceInDockerImages = new LinkedHashSet<>();

    public ToolInfoImpl(String name) {
        this(name, new LinkedHashMap<>(), new LinkedHashSet<>());
    }

    private ToolInfoImpl(String name, Map<String, CommandTargetInfoImpl> commandMap, Set<String> absenceInDockerImages) {
        super();
        this.name = name;
        this.commandMap = commandMap;
        this.absenceInDockerImages = absenceInDockerImages;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Stream<String> getAbsenceInDockerImages() {
        return absenceInDockerImages.stream();
    }

    @Override
    public boolean isAbsentInDockerImage(String dockerImage) {
        return absenceInDockerImages.contains(dockerImage);
    }

    public void setAbsentOnHost(Boolean value) {
        absentOnHost = Optional.ofNullable(value);
    }

    @Override
    public Optional<Boolean> getAbsentOnHost() {
        return absentOnHost;
    }

    public ToolInfoImpl setAbsentInDockerImage(String dockerImage) {
        this.absenceInDockerImages.add(dockerImage);
        return this;
    }

    @Override
    public CommandTargetInfoImpl findCommandByImage(String imageName) {
        CommandTargetInfoImpl result = commandMap.values().stream()
            .filter(cpi -> cpi.getDockerImages().anyMatch(img -> img.equals(imageName)))
            .findFirst().orElse(null);
        return result;
    }

    @Override
    public CommandTargetInfoImpl findCommandOnHost() {
        CommandTargetInfoImpl result = commandMap.values().stream()
            .filter(cpi -> Boolean.TRUE.equals(cpi.getAvailableOnHost()))
            .findFirst().orElse(null);
        return result;
    }

//    public Map<String, CommandTargetInfoImpl> getCommandsByPath() {
//        return commandMap;
//    }

//    public Collection<CommandTargetInfoImpl> list() {
//        return commandMap.values();
//    }

    public CommandTargetInfoImpl getOrCreateCommand(String commandPath) {
        return commandMap.computeIfAbsent(commandPath, n -> new CommandTargetInfoImpl(commandPath));
    }

    @Override
    public ToolInfoImpl clone() {
        Map<String, CommandTargetInfoImpl> newCmdMap = commandMap.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().clone(), (u, v) -> u, LinkedHashMap::new));
        return new ToolInfoImpl(name, newCmdMap, new LinkedHashSet<>(absenceInDockerImages));
    }

    @Override
    public String toString() {
        return "ToolInfo [name=" + name + ", commandTargets=" + commandMap.values() + ", absentOnHost=" + absentOnHost +  ", absences: " + absenceInDockerImages + "]";
    }

    @Override
    public Stream<CommandTargetInfo> list() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<CommandTargetInfo> getCommand(String commandPath) {
        // TODO Auto-generated method stub
        return Optional.empty();
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
