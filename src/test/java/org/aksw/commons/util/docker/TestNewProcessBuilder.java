package org.aksw.commons.util.docker;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import org.aksw.shellgebra.exec.graph.NativeExecCxt;
import org.aksw.shellgebra.exec.graph.PGroup;

public class TestNewProcessBuilder {
    @Test
    public void test01() throws Exception {
        Path basePath = Files.createTempDirectory("process-exec-");
        // logger.info("Created path at  " + basePath);
        System.out.println("Created path at  " + basePath);
        try (NativeExecCxt ncxt = NativeExecCxt.create(basePath, true, true, true)) {
            ncxt.setOutputReader(in -> PGroup.readLines(in, line -> System.err.println(line)));
            ncxt.setErrorReader(in -> PGroup.readLines(in, line -> System.err.println(line)));

            ncxt.setInputSupplier(out -> {
                try (PrintStream pout = new PrintStream(out, false, StandardCharsets.UTF_8)) {
                    for (int i = 0; i < 1000; ++i) {
                        pout.println("" + i);
                    }
                }
            }).start();

            ncxt.configure(new ProcessBuilder("head", "-n 2")).start().waitFor();
            ncxt.configure(new ProcessBuilder("head", "-n 2")).start().waitFor();
            System.out.println("Process terminated.");
        } finally {
            Files.deleteIfExists(basePath);
        }
    }
}
