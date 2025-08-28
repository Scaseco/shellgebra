package org.aksw.commons.util.docker;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArgPath;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectFile;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectFile.OpenMode;
import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.exec.CmdOpRewriter;
import org.aksw.shellgebra.exec.ExecFactory;
import org.aksw.shellgebra.exec.Execs;
import org.aksw.shellgebra.exec.SysRuntime;
import org.junit.Assert;
import org.junit.Test;

import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Volume;

/**
 * Test rewrite of command expressions to be run in containers.
 * The rewrite creates docker BIND mappings for the involved files.
 */
public class TestCmdToDocker {
    @Test
    public void testBindFiles() {
        CmdOp cmdOp = new CmdOpPipeline(
            new CmdOpExec("/usr/bin/cat", new CmdArgPath("/host/bar.bz2")),
            new CmdOpExec("/usr/bin/lbzip -dc", List.of(), List.of(RedirectFile.fileToStdOut("/host/out", OpenMode.WRITE_TRUNCATE))));

        FileMapper fileMapper = FileMapper.of("/shared");

        // If we run in a container, we need to know a location that is mounted on the host so that it can be
        // shared with other containers.
        CmdOp containerizedCmd = CmdOpRewriter.rewriteForContainer(cmdOp, fileMapper);

        // Assert generated string.
        String expectedStr = "/usr/bin/cat /shared/bar.bz2 | /usr/bin/lbzip -dc >/shared/out";
        String actualStr = SysRuntime.toString(containerizedCmd).scriptString();
        Assert.assertEquals(expectedStr, actualStr);

        // Assert binds.
        List<Bind> expectedBinds = List.of(
            new Bind("/host/bar.bz2", new Volume("/shared/bar.bz2"), AccessMode.ro),
            new Bind("/host/out", new Volume("/shared/out"), AccessMode.rw));
        List<Bind> actualBinds = fileMapper.getBinds();
        Assert.assertEquals(expectedBinds, actualBinds);
    }

    @Test
    public void testExec() throws Exception {
        String expected = "hello";
        FileMapper fileMapper = FileMapper.of("/shared");
        CmdOpExec cmdOp = CmdOpExec.ofLiterals("/usr/bin/printf", "'" + expected + "'");
        ExecFactory factory = Execs.docker("ubuntu:24.04", cmdOp, fileMapper);
        String actual = factory.forNullInput().toByteSource().asCharSource(StandardCharsets.UTF_8).read();
        Assert.assertEquals(expected, actual);
    }
}
