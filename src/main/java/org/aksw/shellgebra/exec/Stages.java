package org.aksw.shellgebra.exec;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.aksw.commons.util.docker.ContainerPathResolver;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.unused.algebra.plan.InputStreamTransform;
import org.aksw.shellgebra.unused.algebra.plan.OutputStreamTransform;
import org.aksw.vshell.registry.CmdOpVisitorExecJvm;
import org.aksw.vshell.registry.JvmCommandRegistry;

public class Stages {
    public static Stage pipeline(Stage ...stages) {
        return pipeline(Arrays.asList(stages));
    }

    public static Stage pipeline(List<Stage> stages) {
        return new StagePipeline(stages);
    }

    public static Stage host(CmdOp cmdOp) {
        return new StageHost(cmdOp);
    }

    public static Stage docker(String imageRef, CmdOp cmdOp, FileMapper fileMapper, Function<CmdOpVar, Stage> varResolver) {
        ContainerPathResolver containerPathResolver = ContainerPathResolver.create();
        return new StageDocker(imageRef, cmdOp, fileMapper, containerPathResolver, varResolver);
    }

    public static Stage jvm(JvmCommandRegistry jvmCmdRegistry, CmdOp cmdOp) {
        // Resolve the cmdOp against the registry.
        CmdOpVisitorExecJvm execVisitor = new CmdOpVisitorExecJvm(jvmCmdRegistry);
        Stage result = cmdOp.accept(execVisitor);
        return result;
    }

    /** Create a stage from the command using the global command registry .*/
    public static Stage jvm(CmdOp cmdOp) {
        JvmCommandRegistry jvmCmdRegistry = JvmCommandRegistry.get();
        Stage result = jvm(jvmCmdRegistry, cmdOp);
        return result;
    }

//    public static Stage javaIn(Function<? super InputStream, ? extends InputStream> transform) {
//        InputStreamTransform t = in -> transform.apply(in);
//        return new JvmStage(t);
//    }

    public static Stage javaIn(InputStreamTransform transform) {
        return new StageJvm(transform);
    }

    public static Stage javaOut(OutputStreamTransform transform) {
        return javaIn(transform.asInputStreamTransform());
    }
}
