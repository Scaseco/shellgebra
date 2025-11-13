package org.aksw.shellgebra.algebra.transform;

import java.util.List;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;

public class UserDefinedCommandDefinition {
    private String name;
    private CmdOp expr;
    private List<CmdOpVar> argList;

    public UserDefinedCommandDefinition(String name, CmdOp expr, List<CmdOpVar> argList) {
        super();
        this.name = name;
        this.expr = expr;
        this.argList = argList;
    }


}
