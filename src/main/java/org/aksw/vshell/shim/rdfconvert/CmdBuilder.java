package org.aksw.vshell.shim.rdfconvert;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Fluent-style builder to build a plain list of strings.
 */
public class CmdBuilder {
    private final List<String> args = new ArrayList<>();

    /**
     * Add name and value iff value is non null.
     * Does nothing if value is null.
     */
    public CmdBuilder opt(String name, String value) {
        Objects.requireNonNull(name);
        if (value != null) {
            args.add(name);
            args.add(value);
        }
        return this;
    }

    public CmdBuilder ifTrue(boolean value, String arg) {
        Objects.requireNonNull(arg);
        if (value) {
            args.add(arg);
        }
        return this;
    }

    public CmdBuilder arg(String value) {
        if (value != null) args.add(value);
        return this;
    }

    public CmdBuilder arg(String value, String fallback) {
        if (value != null) {
            args.add(value);
        } else {
            if (fallback != null) {
                args.add(fallback);
            }
        }
        return this;
    }

    public CmdBuilder args(List<String> list) {
        if (list != null) {
            args.addAll(list);
        }
        return this;
    }

    public List<String> build() { return args; }

    public static CmdBuilder newBuilder() {
        return new CmdBuilder();
    }
}
