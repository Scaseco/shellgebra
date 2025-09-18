package org.aksw.shellgebra.registry.tool;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import org.aksw.shellgebra.registry.tool.model.CommandTargetInfo;

public class CommandTargetInfoImpl
    implements CommandTargetInfo
{
    protected String command;
    protected Optional<Boolean> availableOnHost;
    protected Map<String, Boolean> imageToAvailability;

    public CommandTargetInfoImpl(String command) {
        this(command, Optional.of(Boolean.TRUE) , new LinkedHashMap<>());
    }

    private CommandTargetInfoImpl(String command, Optional<Boolean> availableOnHost, Map<String, Boolean> dockerImages) {
        super();
        this.command = command;
        this.availableOnHost = availableOnHost;
        this.imageToAvailability = dockerImages;
    }

    @Override
    public String getCommand() {
        return command;
    }

    public CommandTargetInfoImpl setAvailabilityHost(Boolean availableOnHost) {
        this.availableOnHost = Optional.ofNullable(availableOnHost);
        return this;
    }

    /** Whether the command may be run on the host. */
    @Override
    public Optional<Boolean> getAvailableOnHost() {
        return availableOnHost;
    }

    @Override
    public Stream<String> getDockerImages() {
        return imageToAvailability.keySet().stream();
    }

    @Override
    public Stream<String> getAvailableImages() {
        return imageToAvailability.entrySet().stream().filter(e -> Boolean.TRUE.equals(e.getValue())).map(Entry::getKey);
    }

    public CommandTargetInfoImpl setDockerImageAvailability(String imageName, Boolean value) {
        imageToAvailability.put(imageName, value);
        return this;
    }

    public CommandTargetInfoImpl addAvailabilityDockerImage(String imageName) {
        setDockerImageAvailability(imageName, true);
        return this;
    }

    @Override
    public Optional<Boolean> getDockerImageAvailability(String imageName) {
        return Optional.ofNullable(imageToAvailability.get(imageName));
    }

    @Override
    public CommandTargetInfoImpl clone() {
        return new CommandTargetInfoImpl(command, availableOnHost, new LinkedHashMap<>(imageToAvailability));
    }

    @Override
    public String toString() {
        return "CommandPathInfo [command=" + command + ", availableOnHost=" + availableOnHost + ", dockerImages=" + imageToAvailability
                + "]";
    }

//    public static class Builder {
//        protected String command;
//        protected boolean allowHost = true;
//        protected Set<String> dockerImageNames = new LinkedHashSet<>();
//
//        public Builder setCommand(String command) {
//            this.command = command;
//            return this;
//        }
//
//        public Builder setAllowHost(boolean allowHost) {
//            this.allowHost = allowHost;
//            return this;
//        }
//
//        public Builder addDockerImageName(String imageName) {
//            dockerImageNames.add(imageName);
//            return this;
//        }
//
//        public Builder addDockerImageNames(Collection<String> imageNames) {
//            imageNames.forEach(this::addDockerImageName);
//            return this;
//        }
//
//        public CommandPathInfo build() {
//            return new CommandPathInfo(command, allowHost, Collections.unmodifiableList(new ArrayList<>(dockerImageNames)));
//        }
//    }
//
//    public static Builder newBuilder() {
//        return new Builder();
//    }
//
//    public static Builder newBuilder(CommandPathInfo cpi) {
//        return newBuilder()
//            .setCommand(cpi.getCommand())
//            .setAllowHost(cpi.isAllowHost())
//            .addDockerImageNames(cpi.getDockerImages());
//    }
}
