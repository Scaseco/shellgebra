package org.aksw.shellgebra.algebra.cmd.op;

import java.util.List;

public interface CmdOpN
    extends CmdOp
{
    List<CmdOp> getSubOps();
}
