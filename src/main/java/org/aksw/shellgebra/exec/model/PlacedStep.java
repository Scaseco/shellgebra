package org.aksw.shellgebra.exec.model;

import java.util.Objects;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;

public class PlacedStep {
    private ExecSite execSite;
    private CmdOp op;

    public PlacedStep(ExecSite execSite, CmdOp op) {
        super();
        this.execSite = Objects.requireNonNull(execSite);
        this.op = Objects.requireNonNull(op);
    }

    public ExecSite getExecSite() {
        return execSite;
    }

    public CmdOp getOp() {
        return op;
    }
}
