package org.aksw.vshell.registry;

import java.util.Map;

import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.algebra.cmd.op.placed.PlacedCmdOp.PlacedCmd;

public record FinalPlacement(PlacedCmd cmdOp, Map<CmdOpVar, PlacedCmd> placements) {}
