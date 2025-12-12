package org.aksw.shellgebra.exec.invocation;

import java.util.List;

/**
 * Argv with resources.
 * e.g., delete temp files on close.
 */
public interface ExecutableInvocation
    extends AutoCloseable
{
    List<String> argv();
}
