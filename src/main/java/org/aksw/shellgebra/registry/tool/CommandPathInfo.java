package org.aksw.shellgebra.registry.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CommandPathInfo {
    protected String command;
    protected List<String> dockerImages;

    private CommandPathInfo(String command, List<String> dockerImages) {
        super();
        this.command = command;
        this.dockerImages = dockerImages;
    }

    public String getCommand() {
        return command;
    }

    public List<String> getDockerImages() {
        return dockerImages;
    }

    public static class Builder {
        protected String command;
        protected Set<String> dockerImageNames = new LinkedHashSet<>();

        public Builder setCommand(String command) {
            this.command = command;
            return this;
        }

        public Builder addDockerImageName(String imageName) {
            dockerImageNames.add(imageName);
            return this;
        }

        public CommandPathInfo build() {
            return new CommandPathInfo(command, Collections.unmodifiableList(new ArrayList<>(dockerImageNames)));
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }
}
