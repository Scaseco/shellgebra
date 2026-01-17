package org.aksw.commons.util.docker;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.aksw.shellgebra.exec.CommandRunner;
import org.aksw.shellgebra.exec.CommandRunnerWrapperBash;

public class TestCommandWrapperBash {
    @Test
    public void test() {
        CommandRunner<String> base = argv -> Arrays.asList(argv).toString();
        CommandRunner<String> wrapped = new CommandRunnerWrapperBash<>(base, List.of("/bin/bash", "-c"));
        String str = wrapped.call("echo", "hello", "world");
        System.out.println(str);
    }
}
