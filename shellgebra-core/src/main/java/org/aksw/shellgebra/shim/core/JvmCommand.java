package org.aksw.shellgebra.shim.core;

import org.aksw.vshell.registry.JvmCommandExecutable;

public interface JvmCommand
    extends JvmCommandParser, JvmCommandExecutable
    // XXX Perhaps a HasJvmCommandParser interface would be better (a command could then still return itself as the parser)
{
}
