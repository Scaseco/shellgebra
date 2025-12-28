package org.aksw.vshell.registry;

import java.util.Map;

import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.exec.model.PlacedCommand;

public record CandidatePlacement(PlacedCommand root, Map<CmdOpVar, PlacedCommand> placements) {}
