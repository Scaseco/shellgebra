package org.aksw.shellgebra.exec.site;

import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;

interface PlacedCmdProcessor<T> {
    T process(CmdOpExec op, T input);
    T process(CmdOpPipeline op, T input);
    T process(CmdOpGroup op, T input);
    T process(CmdOpVar op, T input);
}