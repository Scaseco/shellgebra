package org.aksw.shellgebra.exec.invocation;

import java.util.List;

/**
 * Argument vector (argv) with resources, such as for deleting temporary files on close.
 * Temporary files may e.g. be created as part of process substitution.
 */
public interface ExecutableInvocation
    extends AutoCloseable
{
    List<String> argv();
}
