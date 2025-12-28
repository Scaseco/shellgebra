package org.aksw.shellgebra.exec.model;

import java.util.Set;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;

/**
 * Captures a set of candidate execution sites for a given CmdOp instance.
 * The CmdOp may make use of CmdOpVar to refer to sub-expressions with a different candidate placement.
 */
public record PlacedCommand(CmdOp cmdOp, Set<ExecSite> execSites) {}
