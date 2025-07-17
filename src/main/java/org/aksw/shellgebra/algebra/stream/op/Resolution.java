package org.aksw.shellgebra.algebra.stream.op;

import org.aksw.shellgebra.registry.tool.ToolInfoProviderImpl;
import org.aksw.shellgebra.unused.algebra.plan.InputStreamTransform;
import org.aksw.shellgebra.unused.algebra.plan.OutputStreamTransform;

public class Resolution {
    // protected List<CommandEnv> hostCommands = new ArrayList<>();
    // protected List<CommandEnvDocker> dockerCommands = new ArrayList<>();
    // protected ToolInfo toolInfo;
    protected ToolInfoProviderImpl tools = new ToolInfoProviderImpl();


    // protected Map<String, CommandEnv> dockerCommands; // grouped by image
    protected InputStreamTransform inputStreamTransform;
    protected OutputStreamTransform outputStreamTarsform;

    public ToolInfoProviderImpl getTools() {
        return tools;
    }

//    public List<CommandEnv> getHostCommands() {
//        return hostCommands;
//    }
//
//    public List<CommandEnvDocker> getDockerCommands() {
//        return dockerCommands;
//    }

    public InputStreamTransform getInputStreamTransform() {
        return inputStreamTransform;
    }

    public OutputStreamTransform getOutputStreamTarsform() {
        return outputStreamTarsform;
    }

    public void setInputStreamTransform(InputStreamTransform inputStreamTransform) {
        this.inputStreamTransform = inputStreamTransform;
    }

    public void setOutputStreamTransform(OutputStreamTransform outputStreamTarsform) {
        this.outputStreamTarsform = outputStreamTarsform;
    }

    @Override
    public String toString() {
        return "StreamOpResolution [inputStreamTransform=" + inputStreamTransform + ", outputStreamTarsform="
                + outputStreamTarsform + ", tools=" + tools + "]";
    }
}
