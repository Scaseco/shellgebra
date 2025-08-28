package org.aksw.shellgebra.exec;

import java.util.Arrays;
import java.util.List;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.unused.algebra.plan.InputStreamTransform;
import org.aksw.shellgebra.unused.algebra.plan.OutputStreamTransform;

public class Execs {
    public static ExecFactory pipeline(ExecFactory ...execFactories) {
        return ExecFactoryPipeline.of(Arrays.asList(execFactories));
    }

    public static ExecFactory pipeline(List<ExecFactory> execFactories) {
        return ExecFactoryPipeline.of(execFactories);
    }

    public static ExecFactory host(CmdOp cmdOp) {
        return ExecFactoryHost.of(cmdOp);
    }

    public static ExecFactory docker(String imageRef, CmdOp cmdOp, FileMapper fileMapper) {
        return ExecFactoryDocker.of(imageRef, cmdOp, fileMapper);
    }

    public static ExecFactory javaIn(InputStreamTransform transform) {
        return ExecFactoryInputStreamTransform.of(transform);
    }

    public static ExecFactory javaOut(OutputStreamTransform transform) {
        return javaIn(transform.asInputStreamTransform());
    }
}
