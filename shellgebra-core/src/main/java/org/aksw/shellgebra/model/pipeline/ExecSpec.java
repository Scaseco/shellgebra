package org.aksw.shellgebra.model.pipeline;

import java.util.List;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.exec.FileWriterTask;

public interface ExecSpec {
    CmdOp getCmdOp();

    // The file writers that need to be started so that cmd op can run in the given container.
    List<FileWriterTask> getFileWriterTasks();
}
