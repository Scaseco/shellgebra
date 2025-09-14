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
    CmdBuilder opt(String name, String value) {
        Objects.requireNonNull(name);
        if (value != null) {
            args.add(name);
            args.add(value);
        }
        return this;
    }

    CmdBuilder arg(String value) {
        if (value != null) args.add(value);
        return this;
    }

    List<String> build() { return args; }
}
