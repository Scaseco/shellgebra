package org.aksw.vshell.registry;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// TODO Consolidate with ProcessRunner and JvmExecCxt
public class JvmContext {
    private JvmCommandRegistry jvmCmdRegistry;
    private Map<String, String> environment = new ConcurrentHashMap<>();
    private Path directory;
    private PrintStream out = System.out;
    private PrintStream err = System.err;

    public JvmContext(JvmCommandRegistry jvmCmdRegistry) {
        super();
        this.jvmCmdRegistry = jvmCmdRegistry;
    }

    public JvmCommandRegistry getJvmCmdRegistry() {
        return jvmCmdRegistry;
    }

    public void setJvmCmdRegistry(JvmCommandRegistry jvmCmdRegistry) {
        this.jvmCmdRegistry = jvmCmdRegistry;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    public Path getDirectory() {
        return directory;
    }

    public void setDirectory(Path directory) {
        this.directory = directory;
    }

    public PrintStream getOut() {
        return out;
    }

    public void setOut(PrintStream out) {
        this.out = out;
    }

    public PrintStream getErr() {
        return err;
    }

    public void setErr(PrintStream err) {
        this.err = err;
    }
}
