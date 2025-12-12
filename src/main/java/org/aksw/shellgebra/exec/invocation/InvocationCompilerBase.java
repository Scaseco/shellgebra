package org.aksw.shellgebra.exec.invocation;

import java.io.IOException;

/**
 * Base class that passes on argv.
 */
public abstract class InvocationCompilerBase
    implements InvocationCompiler
{
    @Override
    public final ExecutableInvocation compile(Invocation inv, CompileContext cxt) throws IOException {
        ExecutableInvocation result;
        if (inv instanceof Invocation.Argv a) {
            result = compile(a, cxt);
        } else if (inv instanceof Invocation.Script s) {
            result = compile(s, cxt);
        } else {
            throw new IllegalArgumentException("Unexpected invocation type: " + inv);
        }

//    	ExecutableInvocation result = switch (inv) {
//        case Invocation.Argv a -> compile(a, cxt);
//        case Invocation.Script s -> compile(s, cxt);
//        };
        return result;
    }

    public ExecutableInvocation compile(Invocation.Argv argv, CompileContext ctx) {
        return new ExecutableInvocationSimple(argv.argv());
    }

    public abstract ExecutableInvocation compile(Invocation.Script script, CompileContext ctx);
}
