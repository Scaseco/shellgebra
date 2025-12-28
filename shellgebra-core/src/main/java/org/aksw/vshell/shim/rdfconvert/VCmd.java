package org.aksw.vshell.shim.rdfconvert;

/** A virtual command. */
public interface VCmd {
    /** Return an argument vector */
    ArgumentVector toArgv();
}
