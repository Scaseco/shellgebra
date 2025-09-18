package org.aksw.shellgebra.algebra.stream.op;

import org.aksw.shellgebra.algebra.stream.transformer.StreamOpTransformBase;
import org.aksw.shellgebra.registry.tool.ToolInfoImpl;
import org.aksw.shellgebra.registry.tool.ToolInfoProviderImpl;

public class StreamOpTransformToolUsage
    extends StreamOpTransformBase
{
    protected ToolInfoProviderImpl tools = new ToolInfoProviderImpl();

    public ToolInfoProviderImpl getTools() {
        return tools;
    }

    @Override
    public StreamOp transform(StreamOpResolution op, StreamOp subOp) {
        for (ToolInfoImpl toolInfo : op.getResolution().getTools().list()) {
            // tools.getOrCreate(toolInfo.getName())
            tools.merge(toolInfo);
        }
        // return op.copy(subOp);
        return new StreamOpResolution(op.resolution, subOp);
    }
}
