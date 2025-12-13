package org.aksw.shellgebra.exec.invocation;

import java.util.ArrayList;
import java.util.List;

import org.aksw.shellgebra.exec.invocation.Invocation.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvocationCompilerImpl
    extends InvocationCompilerBase
{
    private static final Logger logger = LoggerFactory.getLogger(InvocationCompilerImpl.class);

    private static InvocationCompilerImpl INSTANCE = null;

    private List<ScriptCompilerProvider> providers = new ArrayList<>();

    public static InvocationCompiler getDefault() {
        if (INSTANCE == null) {
            synchronized (InvocationCompilerImpl.class) {
                if (INSTANCE == null) {
                    INSTANCE = new InvocationCompilerImpl();
                    initProviders(INSTANCE);
                }
            }
        }
        return INSTANCE;
    }

    public InvocationCompilerImpl addProvider(ScriptCompilerProvider provider) {
        this.providers.add(0, provider);
        return this;
    }

    public InvocationCompilerImpl addProvider(List<String> mediatTypes, ScriptCompiler provider) {
        addProvider(ScriptCompilerProviderImpl.of(mediatTypes, provider));
        return this;
    }

    public static void initProviders(InvocationCompilerImpl compiler) {
        compiler.addProvider(
            List.of(ScriptContent.contentTypeShellScript, ScriptContent.contentTypeBash),
            (content, mediaType, cxt) -> {
                String scriptRunnerCmd = cxt.getResolver().resolve("bash");
                return new ExecutableInvocationSimple(List.of(scriptRunnerCmd, "-c", content));
            }
        );
    }

    @Override
    public ExecutableInvocation compile(Script script, CompileContext ctx) {
        String mediaType = script.mediaType();
        String content = script.content();

        ExecutableInvocation result = null;
        for (ScriptCompilerProvider provider : providers) {
            try {
                if (provider.supports(mediaType)) {
                    result = provider.compile(content, mediaType, ctx);
                }
            } catch (Exception e) {
                logger.warn("Exception raised by provider", e);
            }
        }

        if (result == null) {
            throw new RuntimeException("Unsupported media type: " + mediaType);
        }

        return result;
    }
}
