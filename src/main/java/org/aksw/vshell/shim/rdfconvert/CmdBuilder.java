package org.aksw.vshell.shim.rdfconvert;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;

/**
 * Fluent-style builder to build a plain list of strings.
 */
public class CmdBuilder {
    private final List<CmdArg> args = new ArrayList<>();

    /**
     * Add name and value iff value is non null.
     * Does nothing if value is null.
     */
    public CmdBuilder opt(String name, String value) {
        Objects.requireNonNull(name);
        if (value != null) {
            args.add(CmdArg.ofLiteral(name));
            args.add(CmdArg.ofString(value));
        }
        return this;
    }

    public CmdBuilder ifTrue(boolean value, String arg) {
        Objects.requireNonNull(arg);
        if (value) {
            args.add(CmdArg.ofLiteral(arg));
        }
        return this;
    }

    public CmdBuilder arg(String value) {
        if (value != null) args.add(CmdArg.ofString(value));
        return this;
    }

    public CmdBuilder fileOrLiteral(String filename, String fallbackLiteral) {
        Objects.requireNonNull(fallbackLiteral);
        if (filename != null) {
            args.add(CmdArg.ofPathString(filename));
        } else {
            args.add(CmdArg.ofLiteral(fallbackLiteral));
        }
        return this;
    }

    public CmdBuilder arg(String value, String fallback) {
        if (value != null) {
            args.add(CmdArg.ofLiteral(value));
        } else {
            if (fallback != null) {
                args.add(CmdArg.ofLiteral(fallback));
            }
        }
        return this;
    }

    public CmdBuilder args(List<String> list) {
        if (list != null) {
            for (String item : list) {
                args.add(CmdArg.ofLiteral(item));
            }
        }
        return this;
    }

    // public List<String> build() { return args; }
    public ArgumentList build() { return new ArgumentList(args); }

    public static CmdBuilder newBuilder() {
        return new CmdBuilder();
    }
}
