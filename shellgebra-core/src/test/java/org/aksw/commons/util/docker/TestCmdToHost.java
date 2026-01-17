package org.aksw.commons.util.docker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;

import com.google.common.io.ByteSource;

import org.junit.jupiter.api.Test;

import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.exec.stage.Stage;
import org.aksw.shellgebra.exec.stage.Stages;


/**
 * Test rewrite of command expressions to be run in containers.
 * The rewrite creates docker BIND mappings for the involved files.
 */
public class TestCmdToHost {
    @Test
    public void testExec() throws Exception {
        String expected = "hello";
        CmdOpExec cmdOp = CmdOpExec.ofLiterals("/usr/bin/printf", "'" + expected + "'");
        Stage stage = Stages.host(cmdOp);
        String actual = stage.fromNull().toByteSource().asCharSource(StandardCharsets.UTF_8).read();
        assertEquals(expected, actual);
    }

    @Test
    public void testExecFromInputStream() throws Exception {
        String expected = "hello";
        ByteSource byteSource = ByteSource.wrap(expected.getBytes(StandardCharsets.UTF_8));
        CmdOpExec cmdOp = CmdOpExec.ofLiterals("/usr/bin/cat");
        Stage stage = Stages.host(cmdOp);
        String actual = stage.from(byteSource)
                .toByteSource().asCharSource(StandardCharsets.UTF_8).read();
        assertEquals(expected, actual);
    }
}
