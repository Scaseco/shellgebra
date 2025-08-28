package org.aksw.commons.util.docker;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.exec.ExecFactory;
import org.aksw.shellgebra.exec.Execs;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.ByteSource;

public class TestExecFactoryPipeline {
    @Test
    public void test01() throws IOException {
        FileMapper fileMapper = FileMapper.of("/shared");

        String expected = "hello";
        CmdOpExec cmdOp = CmdOpExec.ofLiterals("/usr/bin/printf", "'" + expected + "'");
        ExecFactory base = Execs.host(cmdOp);

        ExecFactory factory = Execs.pipeline(
            Execs.javaOut(BZip2CompressorOutputStream::new),
            Execs.docker("nestio/lbzip2", CmdOpExec.ofLiterals("/usr/bin/lbzip2", "-d"), fileMapper)
        );

        ByteSource bs = factory.forInput(base.forNullInput()).toByteSource();

        String actual = bs.asCharSource(StandardCharsets.UTF_8).read();
        Assert.assertEquals(expected, actual);

        // System.out.println("Base value: " + base.forNullInput().toByteSource().asCharSource(StandardCharsets.UTF_8).read());
    }
}
