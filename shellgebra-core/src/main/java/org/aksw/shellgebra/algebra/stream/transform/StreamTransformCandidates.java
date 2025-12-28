package org.aksw.shellgebra.algebra.stream.transform;

import java.util.List;

import org.aksw.commons.io.util.stream.InputStreamTransform;
import org.aksw.commons.io.util.stream.OutputStreamTransform;
import org.aksw.shellgebra.unused.algebra.plan.CommandEnv;
import org.aksw.shellgebra.unused.algebra.plan.CommandEnvDocker;

public class StreamTransformCandidates {
    protected List<CommandEnv> hostCommands;
    protected List<CommandEnvDocker> dockerCommands;
    protected InputStreamTransform inputStreamTransform;
    protected OutputStreamTransform outputStreamTransform;
}
