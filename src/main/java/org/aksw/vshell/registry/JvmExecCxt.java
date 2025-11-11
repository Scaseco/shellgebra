package org.aksw.vshell.registry;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Map;

public class JvmExecCxt {
    private JvmContext context; // Used executor + JvmCommandRegistry registry;

    // private List<String> command; // Perhaps use Argv?
    private Map<String, String> environment;
    private Path directory;

    private InputStream inputStream;
    private PrintStream outputStream;
    private PrintStream errorStream;

    public JvmExecCxt(
            JvmContext context,
            // List<String> command,
            Map<String, String> environment,
            Path directory,
            InputStream inputStream, PrintStream outputStream, PrintStream errorStream) {
        super();
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.errorStream = errorStream;
        // this.command = command;
        this.environment = environment;
        this.directory = directory;
        this.context = context;
    }

//    public List<String> command() {
//        return command;
//    }

    public InputStream in() {
        return inputStream;
    }

    public PrintStream out() {
        return outputStream;
    }

    public PrintStream err() {
        return errorStream;
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

    public JvmContext context() {
        return context;
    }
}
