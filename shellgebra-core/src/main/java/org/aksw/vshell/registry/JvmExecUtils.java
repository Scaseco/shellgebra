package org.aksw.vshell.registry;

public class JvmExecUtils {
    public static String removeTrailingNewline(String value) {
        return value.replaceAll("\n$", "");
    }
}
