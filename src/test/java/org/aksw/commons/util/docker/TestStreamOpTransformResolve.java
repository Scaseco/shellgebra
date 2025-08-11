package org.aksw.commons.util.docker;

import org.aksw.shellgebra.algebra.common.TranscodeMode;
import org.aksw.shellgebra.algebra.stream.op.StreamOp;
import org.aksw.shellgebra.algebra.stream.op.StreamOpContentConvert;
import org.aksw.shellgebra.algebra.stream.op.StreamOpFile;
import org.aksw.shellgebra.algebra.stream.op.StreamOpTranscode;
import org.aksw.shellgebra.algebra.stream.op.StreamOpTransformResolve;
import org.aksw.shellgebra.algebra.stream.op.ToolUsage;
import org.aksw.shellgebra.algebra.stream.transformer.StreamOpTransformer;
import org.aksw.shellgebra.registry.tool.ToolInfoProviderImpl;
import org.aksw.shellgebra.unused.algebra.dag.OpSpecEdge;
import org.aksw.shellgebra.unused.algebra.dag.OpSpecNode;
import org.aksw.shellgebra.unused.algebra.dag.StreamOpVisitorToGraph;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.junit.Test;

public class TestStreamOpTransformResolve {
    @Test
    public void test() {
        StreamOpTransformResolve resolveTransform = new StreamOpTransformResolve();

        StreamOp op =
            new StreamOpTranscode(TranscodeMode.DECODE, "gz",
                new StreamOpContentConvert("rdfxml", "nt", null,
                    new StreamOpTranscode(TranscodeMode.ENCODE, "bzip2", new StreamOpFile("/tmp/foo.rdf.bz2"))));


        StreamOpVisitorToGraph graphConverter = new StreamOpVisitorToGraph();
        op.accept(graphConverter);
        DirectedAcyclicGraph<OpSpecNode, OpSpecEdge> dag = graphConverter.getDag();

        System.out.println(dag);
        if (true) { return; }


        StreamOp afterOp = StreamOpTransformer.transform(op, resolveTransform);
        System.out.println("Resolve transform outcome: " + afterOp);

        // For each node, determine which tools and images could be used.
        ToolInfoProviderImpl tools = ToolUsage.analyzeUsage(afterOp);

        // Check if the tools also exist in other images.
        ToolUsage.enrich(tools, "adfreiburg/qlever:commit-f59763c");

        // Note: Image vs Container: Do we need to explicitly model when to start a new container and when to exec
        // inside an existing one? I think here we only schedule new containers to start - so image names are sufficient.



        // Next step: Execution Placement - place operations on execution sites.
        // Top-down traverse the op and try to map as many tools as possible into a specified target container.

        // The arg builder approach right now only works for stdin/stdout based streams.
        // Instead of Strings, the arg builder could emit "Arg" objects - there could be an INPUT and OUTPUT token.
        // This could be substituted later - so its like a variable substitution then.
        // Under this perspective, ArgBuilder should probably just emit a List<CmdArg>.
        // CmdArg: String, Variable,  - perhaps even support CmdArgOverCmdOp.

//        Tool x = null;
//        x.argsBuilder();

        System.out.println(tools);
    }
}
