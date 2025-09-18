package org.aksw.vshell.shim.rdfconvert;

/** Interface for domain implementations that can be converted to argument lists. */
public interface Args {
    ArgumentList toArgList();
}
