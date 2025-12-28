package org.aksw.shellgebra.exec;

import java.util.List;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;

// FIXME Probably this class is not needed:
// We don't need a record to store command + file writers - but instead we need a rewriter
// that can rewrite any command such that it can run on a container.
public record Command(CmdOp baseCmdOp, List<FileWriterTask> fileWriterTasks) {}
