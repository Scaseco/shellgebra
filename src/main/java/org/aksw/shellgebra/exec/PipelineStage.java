package org.aksw.shellgebra.exec;

import java.util.List;

import com.google.common.io.ByteSource;

public class PipelineStage
    implements Stage
{
    private List<Stage> stages;

    public PipelineStage(List<Stage> stages) {
        super();
        this.stages = stages;
    }

    @Override
    public BoundStage from(ByteSource input) {
        BoundStage result = null;
        for (Stage f : stages) {
            result = (result == null)
                ? f.from(input)
                : f.from(result);
        }
        return result;
    }

    @Override
    public BoundStage from(FileWriterTask input) {
        BoundStage result = null;
        for (Stage f : stages) {
            result = (result == null)
                ? f.from(input)
                : f.from(result);
        }
        return result;
    }

    @Override
    public BoundStage from(BoundStage input) {
        BoundStage result = null;
        for (Stage f : stages) {
            result = (result == null)
                ? f.from(input)
                : f.from(result);
        }
        return result;
    }

    @Override
    public BoundStage fromNull() {
        BoundStage result = null;
        for (Stage f : stages) {
            result = (result == null)
                ? f.fromNull()
                : f.from(result);
        }
        return result;
    }
}
