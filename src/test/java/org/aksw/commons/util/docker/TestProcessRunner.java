package org.aksw.commons.util.docker;

import org.junit.Test;

import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestProcessRunner {
    private static final Logger logger = LoggerFactory.getLogger(TestProcessRunner.class);

    @Test
    public void test01() throws Exception {
        try (ProcessRunner runner = ProcessRunner.create()) {
            runner.setOutputLineReaderUtf8(logger::info);
            runner.setErrorLineReaderUtf8(logger::info);
            runner.setInputPrintStreamUtf8(out -> {
                for (int i = 0; i < 1000; ++i) {
                    out.println("" + i);
                }
            });
            runner.start(new ProcessBuilder("head", "-n 2")).waitFor();
            runner.start(new ProcessBuilder("head", "-n 2")).waitFor();
        }
    }
}
