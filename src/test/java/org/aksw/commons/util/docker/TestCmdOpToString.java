package org.aksw.commons.util.docker;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArgLiteral;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgPath;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.cmd.transform.CmdString;
import org.aksw.shellgebra.exec.SysRuntime;
import org.aksw.shellgebra.exec.SysRuntimeImpl;
import org.junit.Assert;
import org.junit.Test;

public class TestCmdOpToString {
    @Test
    public void testBashPipe() {
        CmdOp cmdOp = new CmdOpPipeline(
            new CmdOpExec("/my/tool.sh", new CmdArgLiteral("-f"), new CmdArgPath("/foo/bar.dat")),
            new CmdOpExec("/my/conv.sh", new CmdArgLiteral("-convert")));

        SysRuntime runtime = SysRuntimeImpl.forCurrentOs();
        CmdString cmdString = runtime.compileString(cmdOp);
        String actualScriptString = cmdString.scriptString();

        String expectedScriptString = "/my/tool.sh -f /foo/bar.dat | /my/conv.sh -convert";
        Assert.assertEquals(expectedScriptString, actualScriptString);

//        List<String> expected = List.of("/usr/bin/bash", "-c", "/my/tool.sh -f /foo/bar.dat | /my/conv.sh -convert");
//        List<String> actual = List.of(SysRuntimeImpl.forCurrentOs().compileCommand(cmdOp));
//        Assert.assertEquals(expected, actual);
    }
}
