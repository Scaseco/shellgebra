package org.aksw.shellgebra.exec.model;

import java.util.Map;

import org.aksw.shellgebra.algebra.stream.op.StreamOp;

public class DockerNode {
    protected String imageName;
    protected String entryPoint;
    protected Map<String, String> mountMap; // perhaps include ro flags

    /** UID:GID string */
    // XXX Use separate ints?
    protected String user;
    // protected CmdOp commandOp;
    // Map stream variables to input files?!
    protected StreamOp streamOp; // The process to be executed in this container.
}
