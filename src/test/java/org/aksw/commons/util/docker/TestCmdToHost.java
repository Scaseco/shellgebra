package org.aksw.commons.util.docker;

import java.nio.charset.StandardCharsets;

import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.exec.ExecFactory;
import org.aksw.shellgebra.exec.Execs;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.ByteSource;


/**
 * Test rewrite of command expressions to be run in containers.
 * The rewrite creates docker BIND mappings for the involved files.
 */
public class TestCmdToHost {
    @Test
    public void testExec() throws Exception {
        String expected = "hello";
        CmdOpExec cmdOp = CmdOpExec.ofLiterals("/usr/bin/printf", "'" + expected + "'");
        ExecFactory factory = Execs.host(cmdOp);
        String actual = factory.forNullInput().toByteSource().asCharSource(StandardCharsets.UTF_8).read();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testExecFromInputStream() throws Exception {
        String expected = "hello";
        ByteSource byteSource = ByteSource.wrap(expected.getBytes(StandardCharsets.UTF_8));
        CmdOpExec cmdOp = CmdOpExec.ofLiterals("/usr/bin/cat");
        ExecFactory factory = Execs.host(cmdOp);
        String actual = factory.forInput(byteSource)
                .toByteSource().asCharSource(StandardCharsets.UTF_8).read();
        Assert.assertEquals(expected, actual);
    }
}
