package org.aksw.shellgebra.exec.model;

import java.util.Set;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;

public record PlacedCommand(CmdOp cmdOp, Set<ExecSite> execSites) {}
