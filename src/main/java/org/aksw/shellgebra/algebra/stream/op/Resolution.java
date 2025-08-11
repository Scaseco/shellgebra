package org.aksw.shellgebra.algebra.stream.op;

import org.aksw.shellgebra.registry.tool.ToolInfoProviderImpl;
import org.aksw.shellgebra.unused.algebra.plan.InputStreamTransform;
import org.aksw.shellgebra.unused.algebra.plan.OutputStreamTransform;

/**
 * A resolution represents a concrete set of candidates that implement
 * a byte stream transformation.
 */
public class Resolution {
    // XXX Should keep a reference to the OpSpec.

    protected ToolInfoProviderImpl tools = new ToolInfoProviderImpl();

    protected InputStreamTransform inputStreamTransform;
    protected OutputStreamTransform outputStreamTarsform;

    public ToolInfoProviderImpl getTools() {
        return tools;
    }

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
