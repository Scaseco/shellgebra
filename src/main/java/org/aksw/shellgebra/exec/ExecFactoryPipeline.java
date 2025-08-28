package org.aksw.shellgebra.exec;

import java.util.List;

import com.google.common.io.ByteSource;

public class ExecFactoryPipeline
    implements ExecFactory
{
    private List<ExecFactory> execFactories;

    public ExecFactoryPipeline(List<ExecFactory> execFactories) {
        super();
        this.execFactories = execFactories;
    }

    public static ExecFactory of(List<ExecFactory> execFactories) {
        return new ExecFactoryPipeline(execFactories);
    }

    @Override
    public ExecBuilder forInput(ByteSource input) {
        ExecBuilder result = null;
        for (ExecFactory f : execFactories) {
            result = (result == null)
                ? f.forInput(input)
                : f.forInput(result);
        }
        return result;
    }

    @Override
    public ExecBuilder forInput(FileWriterTask input) {
        ExecBuilder result = null;
        for (ExecFactory f : execFactories) {
            result = (result == null)
                ? f.forInput(input)
                : f.forInput(result);
        }
        return result;
    }

    @Override
    public ExecBuilder forInput(ExecBuilder input) {
        ExecBuilder result = null;
        for (ExecFactory f : execFactories) {
            result = (result == null)
                ? f.forInput(input)
                : f.forInput(result);
        }
        return result;
    }

    @Override
    public ExecBuilder forNullInput() {
        ExecBuilder result = null;
        for (ExecFactory f : execFactories) {
            result = (result == null)
                ? f.forNullInput()
                : f.forInput(result);
        }
        return result;
    }
}


