package org.aksw.shellgebra.algebra.stream.op;

import org.aksw.shellgebra.registry.tool.ToolInfoProviderImpl;
import org.aksw.shellgebra.unused.algebra.plan.InputStreamTransform;
import org.aksw.shellgebra.unused.algebra.plan.OutputStreamTransform;

/**
 * Resolution for unary byte stream operations.
 *
 * A resolution represents a concrete set of candidates that implement
 * a byte stream transformation.
 */
public class Resolution1 {
    // XXX Should keep a reference to the OpSpec.

    protected ToolInfoProviderImpl tools = new ToolInfoProviderImpl();

    protected InputStreamTransform inputStreamTransform;
    protected OutputStreamTransform outputStreamTransform;

    public ToolInfoProviderImpl getTools() {
        return tools;
    }

    public InputStreamTransform getInputStreamTransform() {
        return inputStreamTransform;
    }

    public OutputStreamTransform getOutputStreamTransform() {
        return outputStreamTransform;
    }

    public void setInputStreamTransform(InputStreamTransform inputStreamTransform) {
        this.inputStreamTransform = inputStreamTransform;
    }

    public void setOutputStreamTransform(OutputStreamTransform outputStreamTarsform) {
        this.outputStreamTransform = outputStreamTarsform;
    }

    @Override
    public String toString() {
        return "StreamOpResolution [inputStreamTransform=" + inputStreamTransform + ", outputStreamTransform="
                + outputStreamTransform + ", tools=" + tools + "]";
    }
}
