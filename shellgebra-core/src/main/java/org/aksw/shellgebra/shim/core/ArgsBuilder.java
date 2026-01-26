package org.aksw.shellgebra.shim.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Fluent-style builder to build a plain list of strings.
 *
 * See {@link ArgumentListBuilder} for a shell-level argument builder.
 */
public class ArgsBuilder {
    private final List<String> args = new ArrayList<>();

    /**
     * Add name and value iff value is non null.
     * Does nothing if value is null.
     */
    public ArgsBuilder opt(String name, String value) {
        Objects.requireNonNull(name);
        if (value != null) {
            args.add(name);
            args.add(value);
        }
        return this;
    }

    public ArgsBuilder ifTrue(boolean value, String arg) {
        Objects.requireNonNull(arg);
        if (value) {
            args.add(arg);
        }
        return this;
    }

    public ArgsBuilder arg(String value) {
        if (value != null) args.add(value);
        return this;
    }

    public ArgsBuilder fileOrLiteral(String filename, String fallbackLiteral) {
        Objects.requireNonNull(fallbackLiteral);
        if (filename != null) {
            args.add(filename);
        } else {
            args.add(fallbackLiteral);
        }
        return this;
    }

    public ArgsBuilder arg(String value, String fallback) {
        if (value != null) {
            args.add(value);
        } else {
            if (fallback != null) {
                args.add(fallback);
            }
        }
        return this;
    }

    public ArgsBuilder args(List<String> list) {
        if (list != null) {
            for (String item : list) {
                args.add(item);
            }
        }
        return this;
    }

    /** Note: '-' becomes a literal instead of a path! */
    public ArgsBuilder files(List<String> list) {
        if (list != null) {
            for (String item : list) {
//                String arg = ("-".equals(item))
//                    ? CmdArg.ofLiteral(item)
//                    : CmdArg.ofPathString(item);
                args.add(item);
            }
        }
        return this;
    }

    // public List<String> build() { return args; }
    public List<String> build() { return new ArrayList<>(args); }

    public static ArgsBuilder newBuilder() {
        return new ArgsBuilder();
    }
}
