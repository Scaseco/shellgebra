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

    public static Argv of(List<String> argv) {
        return ofArgs(argv.get(0), argv.subList(1, argv.size()));
    }

    public static Argv of(String... argv) {
        if (argv.length == 0) {
            throw new IllegalArgumentException("At least on item expected.");
        }
        return new Argv(List.of(argv));
    }

    public static Argv ofArgs(String command, String ...args) {
        return ofArgs(command, List.of(args));
    }

    public static Argv ofArgs(String command, List<String> args) {
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

    public String[] newArgs() {
        return args().toArray(String[]::new);
    }
}
