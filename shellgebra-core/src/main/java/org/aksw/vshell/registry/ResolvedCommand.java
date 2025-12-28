package org.aksw.vshell.registry;

import org.aksw.shellgebra.exec.model.ExecSite;

public record ResolvedCommand(String location, ExecSite execSite) {}
