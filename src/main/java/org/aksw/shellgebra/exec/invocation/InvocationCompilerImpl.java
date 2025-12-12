package org.aksw.shellgebra.exec.invocation;

import java.util.List;

import org.aksw.shellgebra.exec.invocation.Invocation.Script;

public class InvocationCompilerImpl
    extends InvocationCompilerBase
{
    private static InvocationCompiler INSTANCE = null;

    public static InvocationCompiler getDefault() {
        if (INSTANCE == null) {
            synchronized (InvocationCompilerImpl.class) {
                if (INSTANCE == null) {
                    INSTANCE = new InvocationCompilerImpl();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public ExecutableInvocation compile(Script script, CompileContext ctx) {
        String mediaType = script.mediaType();
        String content = script.content();

        // TODO Turn this into a provider / registry
        if (List.of(ScriptContent.contentTypeShellScript, ScriptContent.contentTypeBash).contains(mediaType)) {
            String scriptRunnerCmd = ctx.getResolver().resolve("bash");
            return new ExecutableInvocationSimple(List.of(scriptRunnerCmd, "-c", content));
        }

        throw new RuntimeException("Unsupported media type: " + mediaType);
    }
}
