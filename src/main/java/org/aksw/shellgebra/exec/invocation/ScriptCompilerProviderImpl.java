package org.aksw.shellgebra.exec.invocation;

import java.util.List;

public class ScriptCompilerProviderImpl
    implements ScriptCompilerProvider
{
    private List<String> supportedMediaTypes;
    private ScriptCompiler scriptCompiler;

    public ScriptCompilerProviderImpl(List<String> supportedMediaTypes, ScriptCompiler scriptCompiler) {
        super();
        this.supportedMediaTypes = supportedMediaTypes;
        this.scriptCompiler = scriptCompiler;
    }

    @Override
    public boolean supports(String mediaType) {
        return supportedMediaTypes.contains(mediaType);
    }

    @Override
    public ExecutableInvocation compile(String content, String mediaType, CompileContext cxt) {
        return scriptCompiler.compile(content, mediaType, cxt);
    }

    public static ScriptCompilerProvider of(List<String> supportedMediaTypes, ScriptCompiler scriptCompiler) {
        return new ScriptCompilerProviderImpl(supportedMediaTypes, scriptCompiler);
    }
}
