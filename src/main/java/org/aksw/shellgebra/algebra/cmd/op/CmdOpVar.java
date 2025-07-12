package org.aksw.shellgebra.algebra.cmd.op;

import java.util.Objects;

public class CmdOpVar
    extends CmdOp0
{
    protected String name;

    public CmdOpVar(String name) {
        super();
        this.name = name;
    }

    public String getValue() {
        return name;
    }

    @Override
    public <T> T accept(CmdOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CmdOpVar other = (CmdOpVar) obj;
        return Objects.equals(name, other.name);
    }

    @Override
    public String toString() {
        return "(var " + name + ")";
    }
}
