package org.aksw.shellgebra.algebra.stream.transform;

import java.util.List;

import org.aksw.shellgebra.unused.algebra.plan.CommandEnv;
import org.aksw.shellgebra.unused.algebra.plan.CommandEnvDocker;
import org.aksw.shellgebra.unused.algebra.plan.InputStreamTransform;
import org.aksw.shellgebra.unused.algebra.plan.OutputStreamTransform;

public class StreamTransformCandidates {
    protected List<CommandEnv> hostCommands;
    protected List<CommandEnvDocker> dockerCommands;
    protected InputStreamTransform inputStreamTransform;
    protected OutputStreamTransform outputStreamTransform;


}
