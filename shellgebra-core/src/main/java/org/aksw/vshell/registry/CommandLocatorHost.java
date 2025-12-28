package org.aksw.vshell.registry;

import java.io.IOException;
import java.util.Optional;

import org.aksw.shellgebra.exec.SysRuntime;
import org.aksw.shellgebra.exec.SysRuntimeImpl;

public class CommandLocatorHost
    implements CommandLocator
{
    @Override
    public Optional<String> locate(String command) {
        SysRuntime sysRuntime = SysRuntimeImpl.forCurrentOs();
        String result;
        try {
            result = sysRuntime.which(command);
        } catch (IOException | InterruptedException e) {
            result = null;
        }
        return Optional.ofNullable(result);
    }
}
