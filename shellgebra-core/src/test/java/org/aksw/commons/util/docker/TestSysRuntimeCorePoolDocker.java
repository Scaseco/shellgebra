package org.aksw.commons.util.docker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import org.aksw.shellgebra.exec.SysRuntimeCore;
import org.aksw.shellgebra.exec.SysRuntimeCoreFactoryPoolDocker;

public class TestSysRuntimeCorePoolDocker {
    @Test
    public void test01() throws IOException, InterruptedException {
        try (SysRuntimeCoreFactoryPoolDocker pool = SysRuntimeCoreFactoryPoolDocker.of()) {
            try (SysRuntimeCore runtime = pool.getRuntime("adfreiburg/qlever:commit-a307781")) {
                String location = runtime.execCmd("which", "bzip2");
                // System.out.println(location);
                assertEquals("/usr/bin/bzip2", location);
            }
        }
    }
}
