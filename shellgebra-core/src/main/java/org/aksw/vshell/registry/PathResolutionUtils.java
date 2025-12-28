package org.aksw.vshell.registry;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class PathResolutionUtils {
    public static List<String> getPathItems(Map<String, String> env, String pathKey, String pathSeparator) {
        String pathStr = env.getOrDefault(pathKey, "");
        List<String> result = pathStr == null
            ? List.of()
            : List.of(pathStr.split(pathSeparator));
        return result;
    }

    public static Stream<String> streamPathResolutionCandidates(List<String> pathItems, String localName) {
        return pathItems.stream().map(pathStr -> {
            String str = pathStr.endsWith("/")
                    ? pathStr + localName
                    : pathStr + "/" + localName;
            return str;
        });
    }
}
