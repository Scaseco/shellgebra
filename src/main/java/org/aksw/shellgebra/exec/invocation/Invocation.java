package org.aksw.shellgebra.exec.invocation;

import java.util.List;

/**
 * Record that holds either an argv list or a stript string (content + mediaType).
 */
public sealed interface Invocation {
    record Argv(List<String> argv) implements Invocation {
        @Override public boolean isArgv() { return true; }
        @Override public Argv asArgv() { return this; }
    }

    record Script(String content, String mediaType) implements Invocation {
        @Override public boolean isScript() { return true; }
        @Override public Script asScript() { return this; }
    }

    default boolean isArgv() { return false; }
    default Argv asArgv() { throw new UnsupportedOperationException(); }

    default boolean isScript() { return false; }
    default Script asScript() { throw new UnsupportedOperationException(); }
}
