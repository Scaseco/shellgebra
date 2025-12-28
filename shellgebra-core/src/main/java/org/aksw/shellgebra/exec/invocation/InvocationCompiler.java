package org.aksw.shellgebra.exec.invocation;

import java.io.IOException;

public interface InvocationCompiler {
    ExecutableInvocation compile(Invocation inv, CompileContext ctx) throws IOException;
}
