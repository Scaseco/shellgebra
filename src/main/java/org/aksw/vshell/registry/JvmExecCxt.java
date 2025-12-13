package org.aksw.vshell.registry;

import java.nio.file.Path;
import java.util.Map;

import org.aksw.shellgebra.exec.graph.ProcessRunner;

// FIXME Consolidate with ProcessRunner
public class JvmExecCxt {
    private ProcessRunner executor;
    // private JvmContext context; // Used executor + JvmCommandRegistry registry;

    // Track the parent command or process (if exists)?
    //   Might requires wrapped context with additional info.
    // private List<String> command;
    private Map<String, String> environment;
    private Path directory;

    private DynamicInput inputSource;
    private DynamicOutput outputTarget;
    private DynamicOutput errorTarget;

    public JvmExecCxt(
            ProcessRunner executor,
            Map<String, String> environment,
            Path directory,
            DynamicInput inputSource, DynamicOutput outputTarget, DynamicOutput errorTarget) {
        super();
        this.executor = executor;
        this.environment = environment;
        this.directory = directory;
        this.inputSource = inputSource;
        this.outputTarget = outputTarget;
        this.errorTarget = errorTarget;
    }

    public DynamicInput in() {
        return inputSource;
    }

    public DynamicOutput out() {
        return outputTarget;
    }

    public DynamicOutput err() {
        return errorTarget;
    }

    public Map<String, String> env() {
        return environment;
    }

    public Path directory() {
        return directory;
    }

    public ProcessRunner getExecutor() {
        return executor;
    }
}
