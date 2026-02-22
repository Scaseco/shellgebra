package org.aksw.commons.util.docker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import org.aksw.shellgebra.exec.SysRuntime;
import org.aksw.shellgebra.exec.SysRuntimeFactoryDocker;
import org.testcontainers.containers.GenericContainer;


public class TestSysRuntimeDocker {
    // @Test
    public void test00() {
        try (GenericContainer<?> container = new GenericContainer<>("ubuntu:24.04")
                .withCreateContainerCmdModifier(cmd -> cmd.withEntrypoint("sh"))
                .withCommand("-c", "which sh")
                ) {

            container.start();
            int exitCode = container.getCurrentContainerInfo().getState().getExitCodeLong().intValue();
            System.out.println(exitCode);
            // return exitCode;
        }
    }

    @Test
    public void test01() throws IOException, InterruptedException {
        SysRuntimeFactoryDocker f = SysRuntimeFactoryDocker.get();
        String str;
        try (SysRuntime sys = f.create("ubuntu:24.04")) {
            str = sys.which("cat");
            // System.out.println(sys.exists("/foo/bar"));
        }
        assertEquals("/usr/bin/cat", str);
    }

    @Test
    public void test02() throws IOException, InterruptedException {
        SysRuntimeFactoryDocker f = SysRuntimeFactoryDocker.get();
        boolean b;
        try (SysRuntime sys = f.create("ubuntu:24.04")) {
            b = sys.exists("/usr/bin/cat");
        }
        assertTrue(b);
    }
}
