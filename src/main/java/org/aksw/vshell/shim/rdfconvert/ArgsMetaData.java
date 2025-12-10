package org.aksw.vshell.shim.rdfconvert;

// XXX Perhaps we can make this method parts of the Args interface.
// Note, that we could separate args from the tool - so a query for requiresStdIn
//   in a modularized model would require parameters (args, toolId).
// Note, that Args are bound to a specific tool.
public interface ArgsMetaData {
    /** Whether the given arguments would require stdin to be connected. */
    boolean requiresStdIn();
}
