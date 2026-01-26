package org.aksw.shellgebra.shim.core;

import org.aksw.shellgebra.exec.SysRuntime;

public interface ArgsParserProvider {
    JvmCommandParser newParser(SysRuntime runtime, String commandName);
}
