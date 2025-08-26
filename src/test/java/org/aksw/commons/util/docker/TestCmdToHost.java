package org.aksw.commons.util.docker;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.exec.ExecBuilderHost;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.google.common.io.ByteSource;

/**
 * Test rewrite of command expressions to be run in containers.
 * The rewrite creates docker BIND mappings for the involved files.
 */
public class TestCmdToHost {
    @Test
    public void testExec() throws Exception {
        CmdOpExec cmdOp = CmdOpExec.ofLiterals("/usr/bin/printf", "'hello'");
        ExecBuilderHost builder = ExecBuilderHost.of(cmdOp);

        try (InputStream in = builder.asByteSource().openStream()) {
            System.out.println(IOUtils.toString(in, StandardCharsets.UTF_8));
            System.out.println("[done]");
        }
    }

    @Test
    public void testExecFromInputStream() throws Exception {
        ByteSource byteSource = new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                return new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8));
            }
        };

        CmdOpExec cmdOp = CmdOpExec.ofLiterals("/usr/bin/cat");
        ExecBuilderHost builder = ExecBuilderHost.of(cmdOp);

        try (InputStream in = builder.setInputByteSource(byteSource).asByteSource().openStream()) {
            System.out.println(IOUtils.toString(in, StandardCharsets.UTF_8));
            System.out.println("[done]");
        }
    }
}
