package org.aksw.commons.util.docker;

import org.aksw.shellgebra.algebra.common.Transcoding;
import org.aksw.shellgebra.algebra.stream.op.ContentConvertSpec;
import org.aksw.shellgebra.algebra.stream.op.StreamOp;
import org.aksw.shellgebra.algebra.stream.op.StreamOpContentConvert;
import org.aksw.shellgebra.algebra.stream.op.StreamOpFile;
import org.aksw.shellgebra.algebra.stream.op.StreamOpTranscode;
import org.aksw.shellgebra.algebra.stream.op.StreamOpTransformResolve;
import org.aksw.shellgebra.algebra.stream.op.ToolUsage;
import org.aksw.shellgebra.algebra.stream.transformer.StreamOpTransformer;
import org.aksw.shellgebra.registry.tool.ToolInfoProviderImpl;
import org.junit.Test;

public class TestStreamOpTransformResolve {
    @Test
    public void test() {
        StreamOpTransformResolve resolveTransform = new StreamOpTransformResolve();

        StreamOp op =
            new StreamOpTranscode(Transcoding.encode("gz"),
                new StreamOpContentConvert(new ContentConvertSpec("rdfxml", "nt", null),
                        new StreamOpTranscode(Transcoding.decode("bzip2"), new StreamOpFile("/tmp/foo"))));

        StreamOp afterOp = StreamOpTransformer.transform(op, resolveTransform);

        ToolInfoProviderImpl tools = ToolUsage.analyzeUsage(afterOp);
        ToolUsage.enrich(tools, "adfreiburg/qlever:commit-f59763c");

        System.out.println(tools);

    }
}
