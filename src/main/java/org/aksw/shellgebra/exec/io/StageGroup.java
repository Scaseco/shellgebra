package org.aksw.shellgebra.exec.io;

import java.util.List;

import com.google.common.io.ByteSource;

import org.aksw.shellgebra.exec.BoundStage;
import org.aksw.shellgebra.exec.FileWriterTask;
import org.aksw.shellgebra.exec.Stage;

/**
 * input | { x1 ; xi ; xn }
 *
 * The same input is passed to all members of the group.
 */
public class StageGroup
    implements Stage
{
    private List<Stage> stages;

    public StageGroup(List<Stage> stages) {
        super();
        this.stages = stages;
    }


    @Override
    public BoundStage from(ByteSource input) {
        throw new RuntimeException("unsupported");
//        BoundStage result = null;
//        List<ByteSource> byteSources = new ArrayList<>(stages.size());
//        boolean isFirst = true;
//        for (Stage f : stages) {
//            BoundStage boundStage = isFirst
//                ? f.from(input)
//                : f.fromNull();
//        }
//        ByteSource.concat(null)
//        return result;
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
