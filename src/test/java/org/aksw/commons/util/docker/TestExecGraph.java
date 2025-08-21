package org.aksw.commons.util.docker;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.junit.Test;

public class TestExecGraph {

    @Test
    public void testExecGraph() {

        CmdOp conv = CmdOpExec.ofLiterals("/usr/bin/rapper", "-i", "ttl", "-o", "nt");

        CmdOp cat = CmdOpExec.ofLiterals("/usr/bin/cat", "/home/raven/Projects/Eclipse/jenax/norse.ttl");

        CmdOp pipe = new CmdOpPipeline(cat, conv);




        // Create nodes for executing operations in docker containers and in java
        // Set up bind mounts
        // Create a command + file writers that fill the named pipes - and clean up in the end.

    }
}
