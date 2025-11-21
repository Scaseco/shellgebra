package org.aksw.shellgebra.exec.graph;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessCxt
    implements AutoCloseable
{
    private Map<String, String> environment;
    private Path directory;
    private FdTable<FdResource> fdTable;

    public ProcessCxt() {
        this(new ConcurrentHashMap<>(), Path.of(""), new FdTable<>());
    }

    private ProcessCxt(Map<String, String> environment, Path directory, FdTable<FdResource> fdTable) {
        super();
        this.environment = environment;
        this.directory = directory;
        this.fdTable = fdTable;
    }

    public FdTable<FdResource> getFdTable() {
        return fdTable;
    }

    public Path getDirectory() {
        return directory;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public ProcessCxt dup() { // "fork"
        return new ProcessCxt(
                new ConcurrentHashMap<>(environment),
                directory,
                fdTable.dup());
    }

    @Override
    public void close() {
        fdTable.close();
    }
}
