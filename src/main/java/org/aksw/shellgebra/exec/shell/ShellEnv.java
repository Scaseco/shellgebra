package org.aksw.shellgebra.exec.shell;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.aksw.shellgebra.exec.graph.FdTable;
import org.aksw.vshell.registry.PathResolutionUtils;

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

//    protected String getPathValue() {
//        String result = getEnv().getOrDefault(pathKey, "");
//        return result;
//    }

    public List<String> getPathItems() {
        return PathResolutionUtils.getPathItems(env, pathKey, pathSeparator);
    }

    public Stream<String> streamPathCandidates(String localName) {
        List<String> pathItems = getPathItems();
        return PathResolutionUtils.streamPathResolutionCandidates(pathItems, localName);
    }

    @Override
    public void close() throws Exception {
        fdTable.close();
    }
}
