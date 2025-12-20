package org.aksw.vshell.shim.rdfconvert;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

record PathEntry(String path, boolean availability) {}

//class ToolEntry {
//    Map<String, PathEntry> pathEntries = new LinkedHashMap<>();
//
//    public ToolEntry declarePresence(String path) {
//        pathEntries.put(path, new PathEntry(path, true));
//        return this;
//    }
//
//    public ToolEntry declareAbsence(String path) {
//        pathEntries.put(path, new PathEntry(path, false));
//        return this;
//    }
//
////    public Optional<String> getPresent() {
////
////    }
//}


class SystemToolRegistry {
    private Map<String, ToolEntry> toolEntries = new HashMap<>();
}

public class SystemToolCandidateRegistry {
    private Multimap<String, String> candidates = ArrayListMultimap.create();

    public SystemToolCandidateRegistry put(String toolName, String command) {
        candidates.put(toolName, command);
        return this;
    }
}
