package org.aksw.shellgebra.exec.invocation;

import java.nio.file.Path;
import java.util.Map;

public class CompileContext {
    private Path workDir;
    private Map<String, String> env;
    private ExecutableResolver resolver;

    public CompileContext(Path workDir, Map<String, String> env, ExecutableResolver resolver) {
        super();
        this.workDir = workDir;
        this.env = env;
        this.resolver = resolver;
    }

    /** Compile context with a given resolver, current working directory and empty environment. */
    public static CompileContext of(ExecutableResolver resolver) {
        return new CompileContext(Path.of("").toAbsolutePath(), Map.of(), resolver);
    }

    /* Compile context that maps each command name to itself - no resolution. */
    public static CompileContext noResolve() {
        return new CompileContext(Path.of("").toAbsolutePath(), Map.of(), name -> name);
    }

    public Path getWorkDir() {
        return workDir;
    }

    public Map<String, String> getEnv() {
        return env;
    }

    public ExecutableResolver getResolver() {
        return resolver;
    }
}
