package org.aksw.shellgebra.exec.graph;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

public class ProcessGraphImpl
    implements ProcessGraph
{
    private Graph<ConnectNode, DefaultEdge> graph;

    private class ProcessVertex {

    }

    public ProcessGraphImpl() {
        graph = new DirectedAcyclicGraph<>(DefaultEdge.class); //StreamNode.vertexSupplier(), StreamEdge::new, false);
    }

    public class NodeBase {
        private ProcessGraph graph;

        public NodeBase(ProcessGraph graph) {
            super();
            this.graph = graph;
        }

        public ProcessGraph getGraph() {
            return graph;
        }
    }

    public abstract class ProcessNodeBase
        extends NodeBase
        implements ProcessNode {

        private ProcessNode parent;

        public ProcessNodeBase(ProcessGraph graph, ProcessNode parent) {
            super(graph);
            this.parent = parent;
        }

        @Override
        public ProcessNode parent() {
            return parent;
        }
    }

    public class ProcessNodeImpl
        extends ProcessNodeBase
        implements ProcessNode {

        private int id;
        private Callable<List<ProcessBuilder>> processBuilders;

        public ProcessNodeImpl(ProcessGraph graph, ProcessNode parent, Callable<List<ProcessBuilder>> processBuilders) {
            super(graph, parent);
            this.processBuilders = processBuilders;
        }

        @Override
        public ProcessInput in() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProcessOutput out() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProcessOutput err() {
            // TODO Auto-generated method stub
            return null;
        }
    }

    public class PathBase
        implements Pipe {

        @Override
        public ProcessInput getWriteEnd() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProcessOutput getReadEnd() {
            // TODO Auto-generated method stub
            return null;
        }
    }

    public class PipeImpl
        extends PathBase {
    }

    public class PipePathImpl
        extends PathBase
    {
        protected Path path;

        public PipePathImpl(Path path) {
            super();
            this.path = path;
        }
    }

    public class ProcessOutputImpl
    {

    }

    public class ProcessInputImpl
    {

    }

    public class GroupNodeImpl
        extends ProcessNodeImpl
        implements GroupNode
    {
        private List<ProcessNode> members = new ArrayList<>();

        public GroupNodeImpl(ProcessGraph graph, Callable<List<ProcessBuilder>> processBuilders) {
            super(graph, processBuilders);
            // TODO Auto-generated constructor stub
        }

        @Override
        public ProcessNode parent() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public GroupNode add(ProcessNode item) {
            members.add(item);
            return this;
        }
    }

    @Override
    public ProcessNode newProcessNode(Callable<ProcessBuilder> processBuilder) {
        Callable<List<ProcessBuilder>> wrapper = () -> {
            ProcessBuilder item = processBuilder.call();
            return List.of(item);
        };
        ProcessNode result = new ProcessNodeImpl(this, wrapper);

        // Add stdout/stdin/sterr nodes to the graph immediately?
        // Or only track connections in the graph?
        // Probably add immediately, so that we can enumerate them.

        // ProcessOutput stdout = new ProcessOut


        return result;
    }

    protected void addProcessNode(ProcessNode node) {
        node.err();

    }

    protected void removeProcessNode(ProcessNode node) {

    }

    @Override
    public void connect(ProcessOutput source, ProcessInput sink) {
        // graph.ad

        // source.setSink(sink);
    }

    @Override
    public Pipe newPipe() {
        return new PipeImpl();
    }

    @Override
    public Pipe newPath(Path path) {
        return new PipePathImpl(path);
    }



    @Override
    // public List<Process> exec() {
    public void exec() {
        // TODO Auto-generated method stub
        // return null;
    }
}
