package org.aksw.shellgebra.exec.shell;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aksw.shellgebra.exec.io.FdTable;

public class ShellEnv
    implements AutoCloseable
{
    private FdTable fdTable = new FdTable();
    private Map<String, String> env = new ConcurrentHashMap<>();
    // private String cwd;

    public FdTable getFdTable() {
        return fdTable;
    }

    @Override
    public void close() throws Exception {
        fdTable.close();
    }
}
