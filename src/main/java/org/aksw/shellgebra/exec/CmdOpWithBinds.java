package org.aksw.shellgebra.exec;

import java.util.List;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;

import com.github.dockerjava.api.model.Bind;

public record CmdOpWithBinds(CmdOp cmdOp, List<Bind> binds) {}
