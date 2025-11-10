package org.aksw.commons.util.docker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record Argv(List<String> argv) {
    public Argv {
        if (argv.isEmpty()) {
            throw new IllegalArgumentException("At least one element (the command name) expected.");
        }
        argv = List.copyOf(Objects.requireNonNull(argv));
    }

    public static Argv of(String command, String ...args) {
        return of(command, List.of(args));
    }

    public static Argv of(String command, List<String> args) {
        List<String> argv = new ArrayList<>(1 + args.size());
        argv.add(command);
        argv.addAll(args);
        return new Argv(argv);
    }

    public String command() {
        return argv.get(0);
    }

    public List<String> args() {
        return argv.subList(1, argv.size());
    }

    public String[] newArgv() {
        return argv.toArray(String[]::new);
    }
}
