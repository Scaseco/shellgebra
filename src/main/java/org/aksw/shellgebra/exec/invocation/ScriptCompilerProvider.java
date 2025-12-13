package org.aksw.shellgebra.exec.invocation;

public interface ScriptCompilerProvider {
    boolean supports(String mediaType);
    ExecutableInvocation compile(String content, String mediaType, CompileContext cxt);
}
