package org.aksw.shellgebra.registry.tool;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

public class CommandPathInfo {
    protected String command;
    protected Boolean availableOnHost;
    // protected Set<String> dockerImages;
    protected Map<String, Boolean> imageToAvailability;

    public CommandPathInfo(String command) {
        this(command, true, new LinkedHashMap<>());
    }

    private CommandPathInfo(String command, Boolean availableOnHost, Map<String, Boolean> dockerImages) {
        super();
        this.command = command;
        this.availableOnHost = availableOnHost;
        this.imageToAvailability = dockerImages;
    }

    public String getCommand() {
        return command;
    }

    public void setAvailableOnHost(Boolean availableOnHost) {
        this.availableOnHost = availableOnHost;
    }

    /** Whether the command may be run on the host. */
    public Boolean getAvailableOnHost() {
        return availableOnHost;
    }

    public Set<String> getDockerImages() {
        return imageToAvailability.keySet();
    }

    public Stream<String> getAvailableImages() {
        return imageToAvailability.entrySet().stream().filter(e -> Boolean.TRUE.equals(e.getValue())).map(Entry::getKey);
    }

    public CommandPathInfo setDockerImageAvailability(String imageName, Boolean value) {
        imageToAvailability.put(imageName, value);
        return this;
    }

    public CommandPathInfo addDockerImageAvailability(String imageName) {
        setDockerImageAvailability(imageName, true);
        return this;
    }

    public Boolean getDockerImageAvailability(String imageName) {
        return imageToAvailability.get(imageName);
    }

    @Override
    public CommandPathInfo clone() {
        return new CommandPathInfo(command, availableOnHost, new LinkedHashMap<>(imageToAvailability));
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
