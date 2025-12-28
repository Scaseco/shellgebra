package org.aksw.shellgebra.exec.graph;

import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * The process graph is to wire up inputs and outputs.
 * Its on the process level - not on individual statement level that may be executed within a process.
 *
 * Input nodes supply data when executed.
 * They may be based on a process builder or file.
 */
interface InputNode {

}


/**
 * Process graph is for setting up pipelines over processes.
 * its not a staged execution.
 */
public interface ProcessGraph {

    public interface Pipe {
        ProcessInput getWriteEnd();
        ProcessOutput getReadEnd();
    }

    public interface ConnectNode {

    }

    /** A node that is the source of data - such as a file or stdout of a process or the read-end of a pipe. */
    public interface SourceNode
        extends ConnectNode
    {

    }

    public interface SinkNode
        extends ConnectNode
    {
    }


    public interface ProcessInput
        extends SinkNode
    {
        ProcessNode getProcessNode();
        SourceNode getSource();
        ProcessInput setSource(SourceNode sourceNode);
    }

    public interface ProcessOutput
        extends SourceNode
    {
        ProcessNode getProcessNode();
        ProcessOutput setSink(SinkNode sinkNode);
        SourceNode getSink();
    }

    public interface ProcessNode {
        // The immediate parent node that contains this node. Null if there is none.
        ProcessNode parent();

        ProcessInput in();
        ProcessOutput out();
        ProcessOutput err();
    }

    // A Process group
    public interface GroupNode
        extends ProcessNode
    {
        GroupNode add(ProcessNode item);
    }

    public interface PipeNode
        extends ProcessNode
    {
    }

//    public interface ProcessNode
//    	extends ProcessLikeNode
//    {
//
//    }

    // void newPipe();
    // void addFileNode(Path path);
    ProcessNode newProcessNode(Callable<ProcessBuilder> processBuilder);
    Pipe newPipe();

    Pipe newPath(Path path);

    void connect(ProcessOutput source, ProcessInput sink);

    // TODO What to return? Some object that represents an ongoing execution.
    void exec();
}
