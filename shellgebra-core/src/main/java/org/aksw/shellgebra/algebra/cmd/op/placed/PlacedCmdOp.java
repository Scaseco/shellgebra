package org.aksw.shellgebra.algebra.cmd.op.placed;

import java.util.List;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.exec.model.ExecSite;

public sealed interface PlacedCmdOp {
    public interface PlacedCmdOpVisitor<T> {
        T visit(PlacedCmd op);
        T visit(PlacedGroup op);
        T visit(PlacedPipeline op);
    }

    ExecSite getExecSite();
    <T> T accept(PlacedCmdOpVisitor<T> visitor);

    public record PlacedCmd(CmdOp cmdOp, ExecSite execSite) implements PlacedCmdOp {
        @Override public ExecSite getExecSite() { return execSite; }
        @Override public <T> T accept(PlacedCmdOpVisitor<T> visitor) { T result = visitor.visit(this); return result; }
    }

    public record PlacedGroup(List<PlacedCmdOp> subOps, ExecSite execSite) implements PlacedCmdOp {
        @Override public ExecSite getExecSite() { return execSite; }
        @Override public <T> T accept(PlacedCmdOpVisitor<T> visitor) { T result = visitor.visit(this); return result; }
    }

    public record PlacedPipeline(List<PlacedCmdOp> subOps, ExecSite execSite) implements PlacedCmdOp {
        @Override public ExecSite getExecSite() { return execSite; }
        @Override public <T> T accept(PlacedCmdOpVisitor<T> visitor) { T result = visitor.visit(this); return result; }
    }
}
