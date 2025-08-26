package org.aksw.commons.util.docker;

import java.nio.charset.StandardCharsets;

import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.exec.ExecBuilderHost;
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
        ExecBuilderHost builder = ExecBuilderHost.of(cmdOp);
        String actual = builder.asByteSource().asCharSource(StandardCharsets.UTF_8).read();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testExecFromInputStream() throws Exception {
        String expected = "hello";
        ByteSource byteSource = ByteSource.wrap(expected.getBytes(StandardCharsets.UTF_8));
        CmdOpExec cmdOp = CmdOpExec.ofLiterals("/usr/bin/cat");
        ExecBuilderHost builder = ExecBuilderHost.of(cmdOp);
        String actual = builder.setInputByteSource(byteSource)
                .asByteSource().asCharSource(StandardCharsets.UTF_8).read();
        Assert.assertEquals(expected, actual);
    }
}
