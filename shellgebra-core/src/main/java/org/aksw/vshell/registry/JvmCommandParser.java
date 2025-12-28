package org.aksw.vshell.registry;

import org.aksw.vshell.shim.rdfconvert.Args;

public interface JvmCommandParser {
    public Args parseArgs(String... args);
}
