package org.aksw.shellgebra.registry.tool;

import java.util.Optional;

public interface ToolInfoProvider {
    Optional<ToolInfo> get(String toolName);
}
