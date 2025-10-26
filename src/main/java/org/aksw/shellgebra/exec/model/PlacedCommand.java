package org.aksw.shellgebra.exec.model;

import java.util.Objects;
import java.util.Set;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;

public class PlacedCommand {
    private CmdOp op;
    private Set<ExecSite> execSites;

    public PlacedCommand(CmdOp op, Set<ExecSite> execSites) {
        super();
        this.execSites = Objects.requireNonNull(execSites);
        this.op = Objects.requireNonNull(op);
    }

    public Set<ExecSite> getExecSites() {
        return execSites;
    }

    public CmdOp getOp() {
        return op;
    }
}
