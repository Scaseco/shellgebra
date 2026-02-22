package org.aksw.commons.util.docker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Argument vector class. Wraps an list of strings.
 * Provides domain methods to access the command name and argument list.
 * Note, that the command name may be null (sometimes used to represent the null command which does nothing and always succeeds).
 */
public record Argv(List<String> argv) {
    public Argv {
        requireNonEmpty(argv);
        argv = List.copyOf(Objects.requireNonNull(argv));
    }

    // XXX Move to some CollectionUtils class.
    private static void requireNonEmpty(Collection<?> list) {
        Objects.requireNonNull(list);
        if (list.isEmpty()) {
            // XXX Perhaps allow null / empty list for "null command" (in shell this would be ':').
            throw new IllegalArgumentException("At least on item expected.");
        }
    }

    public static Argv of(List<String> argv) {
        return new Argv(argv);
    }

    public static Argv of(String... argv) {
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

    public boolean isEmpty() {
        return argv.isEmpty();
    }

    /** Return the command, or null if this argv instance is empty. */
    public String command() {
        return argv.isEmpty() ? null : argv.get(0);
    }

    public List<String> args() {
        return argv.subList(1, argv.size());
    }

    public String[] toArray() {
        return argv.toArray(String[]::new);
    }

    public String[] argsToArray() {
        return args().toArray(String[]::new);
    }
}
