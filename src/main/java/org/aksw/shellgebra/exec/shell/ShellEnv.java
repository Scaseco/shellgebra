package org.aksw.shellgebra.exec.shell;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.aksw.shellgebra.exec.io.FdTable;

public class ShellEnv
    implements AutoCloseable
{
    private FdTable fdTable = new FdTable();
    private Map<String, String> env = new ConcurrentHashMap<>();
    private String pathSeparator = ":";
    private String pathKey = "PATH";

    public FdTable getFdTable() {
        return fdTable;
    }

    public Map<String, String> getEnv() {
        return env;
    }

    protected String getPathValue() {
        String result = getEnv().getOrDefault(pathKey, "");
        return result;
    }

    public List<String> getPathItems() {
        String pathStr = getPathValue();
        List<String> result = pathStr == null
            ? List.of()
            : List.of(pathStr.split(pathSeparator));
        return result;
    }

    public Stream<String> streamPathCandidates(String localName) {
        return getPathItems().stream().map(pathStr -> {
            String str = pathStr.endsWith("/")
                    ? pathStr + localName
                    : pathStr + "/" + localName;
            return str;
        });
    }

    @Override
    public void close() throws Exception {
        fdTable.close();
    }
}
