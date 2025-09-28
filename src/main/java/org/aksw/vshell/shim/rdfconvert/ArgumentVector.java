package org.aksw.vshell.shim.rdfconvert;

/**
 * An argument vector (often abbreviated as argv).
 * Comprises a program name and arguments.
 */
public record ArgumentVector(String name, Args args) {
    @Override
    public final String toString() {
        ArgumentList argList = args.toArgList();
        return name + " " + argList;
    }
}
