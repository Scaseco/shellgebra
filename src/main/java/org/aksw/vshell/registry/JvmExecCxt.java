package org.aksw.vshell.registry;

import java.nio.file.Path;
import java.util.Map;

import org.aksw.shellgebra.exec.graph.ProcessRunner;

// FIXME Consolidate with ProcessRunner
public class JvmExecCxt {
    private ProcessRunner executor;
    // private JvmContext context; // Used executor + JvmCommandRegistry registry;

    // private List<String> command; // Perhaps use Argv?
    private Map<String, String> environment;
    private Path directory;

    private FileInputSource inputSource;
    private FileOutputTarget outputTarget;
    private FileOutputTarget errorTarget;

    public JvmExecCxt(
            // JvmContext context,
            // List<String> command,
            ProcessRunner executor,
            Map<String, String> environment,
            Path directory,
            FileInputSource inputSource, FileOutputTarget outputTarget, FileOutputTarget errorTarget) {
        super();
        this.inputSource = inputSource;
        this.outputTarget = outputTarget;
        this.errorTarget = errorTarget;
        // this.command = command;
        this.environment = environment;
        this.directory = directory;
        //this.context = context;
    }

//    public List<String> command() {
//        return command;
//    }

//    public InputStream in() {
//        return inputSource.getInputStream();
//    }
//
//    public PrintStream out() {
//        return outputTarget.printer();
//    }
//
//    public PrintStream err() {
//        return errorTarget.printer();
//    }

    public FileInputSource in() {
        return inputSource;
    }

    public FileOutputTarget out() {
        return outputTarget;
    }

    public FileOutputTarget err() {
        return errorTarget;
    }

    public Map<String, String> env() {
        return environment;
    }

//    public void env(Map<String, String> env) {
//        this.environment = env;
//    }

    public Path directory() {
        return directory;
    }

//    public JvmContext context() {
//        return context;
//    }

    public ProcessRunner getExecutor() {
        return executor;
    }
}
