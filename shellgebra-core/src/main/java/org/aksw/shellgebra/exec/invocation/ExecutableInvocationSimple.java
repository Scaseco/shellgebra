package org.aksw.shellgebra.exec.invocation;

import java.util.List;

public record ExecutableInvocationSimple(List<String> argv, AutoCloseable resources)
    implements ExecutableInvocation
{
    public ExecutableInvocationSimple(List<String> argv) {
        this(argv, null);
    }

    @Override
    public void close() throws Exception {
        if (resources != null) {
            resources.close();
        }
    }
}
