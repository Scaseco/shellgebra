package org.aksw.shellgebra.exec;

import java.util.Arrays;
import java.util.List;

import org.aksw.commons.util.docker.ContainerPathResolver;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.unused.algebra.plan.InputStreamTransform;
import org.aksw.shellgebra.unused.algebra.plan.OutputStreamTransform;

public class Stages {
    public static Stage pipeline(Stage ...stages) {
        return pipeline(Arrays.asList(stages));
    }

    public static Stage pipeline(List<Stage> stages) {
        return new PipelineStage(stages);
    }

    public static Stage host(CmdOp cmdOp) {
        return new HostStage(cmdOp);
    }

    public static Stage docker(String imageRef, CmdOp cmdOp, FileMapper fileMapper) {
        ContainerPathResolver containerPathResolver = ContainerPathResolver.create();
        return new DockerStage(imageRef, cmdOp, fileMapper, containerPathResolver);
    }

//    public static Stage javaIn(Function<? super InputStream, ? extends InputStream> transform) {
//        InputStreamTransform t = in -> transform.apply(in);
//        return new JvmStage(t);
//    }

    public static Stage javaIn(InputStreamTransform transform) {
        return new JvmStage(transform);
    }

    public static Stage javaOut(OutputStreamTransform transform) {
        return javaIn(transform.asInputStreamTransform());
    }
}
