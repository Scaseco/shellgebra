package org.aksw.shellgebra.exec.invocation;

import java.util.List;

/**
 * Record that holds either an argv list or a stript string (content + mediaType).
 */
public sealed interface Invocation {
  record Argv(List<String> argv) implements Invocation {}
  record Script(String content, String mediaType) implements Invocation {}
}
