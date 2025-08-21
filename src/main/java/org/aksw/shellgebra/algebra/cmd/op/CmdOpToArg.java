package org.aksw.shellgebra.algebra.cmd.op;

// TODO Deprecate - The proper model is to have an CmdArgWord with a TokenCmdOp.
// Operator to transform a sub op into an argument string
public class CmdOpToArg
    extends CmdOp1
{
    public CmdOpToArg(CmdOp subOp) {
        super(subOp);
    }

    @Override
    public <T> T accept(CmdOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    @Override
    public String toString() {
        return "(toArg " + getSubOp() + ")";
    }
}
