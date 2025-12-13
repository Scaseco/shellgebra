package org.aksw.shellgebra.exec.invocation;

public interface ScriptCompiler {
    ExecutableInvocation compile(String content, String mediaType, CompileContext cxt);
}
