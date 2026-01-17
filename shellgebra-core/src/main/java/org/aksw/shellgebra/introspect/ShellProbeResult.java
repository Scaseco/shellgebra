package org.aksw.shellgebra.introspect;

public record ShellProbeResult(
    String name,
    String location,
    String commandOption, // Alternative: scriptString -> args - Function<String, Args>
    String locatorCommand
)
{
    public static class ShellProbeResultBuilder {
        private String name;
        private String location;
        private String commandOption;
        private String locatorCommand;

        public String getName() {
            return name;
        }

        public ShellProbeResultBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public String getLocatorCommand() {
            return locatorCommand;
        }

        public ShellProbeResultBuilder setLocatorCommand(String locatorCommand) {
            this.locatorCommand = locatorCommand;
            return this;
        }

//        public List<String> getProbeLocations() {
//            return probeLocations;
//        }
//
//        public ShellProbeResultBuilder setProbeLocations(List<String> probeLocations) {
//            this.probeLocations = probeLocations;
//            return this;
//        }

        public String getLocation() {
            return location;
        }

        public ShellProbeResultBuilder setLocation(String location) {
            this.location = location;
            return this;
        }


        public String getCommandOption() {
            return commandOption;
        }

        public ShellProbeResultBuilder setCommandOption(String commandOption) {
            this.commandOption = commandOption;
            return this;
        }

        public ShellProbeResult build() {
            return new ShellProbeResult(name, location, commandOption, locatorCommand);
        }
    }

    public static ShellProbeResultBuilder newBuilder() {
        return new ShellProbeResultBuilder();
    }
}
