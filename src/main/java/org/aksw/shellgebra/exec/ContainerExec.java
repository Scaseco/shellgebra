package org.aksw.shellgebra.exec;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;

import jenax.engine.qlever.docker.ContainerDef;

public class ContainerExec {

    protected ContainerDef containerDef;
    protected CmdOp cmdOp;

    /** File writers that will be started before the actual container is started. */
    protected List<FileWriterTask> inputFileWriterTasks = new ArrayList<>();


    InputStream exec() {
        return null;
    }

    FileWriterTask redirectToFile(Path path) {
        return null;
    }
}
