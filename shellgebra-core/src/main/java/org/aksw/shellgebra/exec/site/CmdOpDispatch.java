package org.aksw.shellgebra.exec.site;

import java.util.ArrayDeque;
import java.util.Deque;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVisitor;

class CmdOpDispatch<T>
    implements CmdOpVisitor<T>
{
    private Deque<T> stack = new ArrayDeque<>();
    private PlacedCmdProcessor<T> processor;

    public T build(CmdOp op, T input) {
        stack.push(input);
        op.accept(this);
        T result = stack.pop();
        return result;
    }

    @Override
    public T visit(CmdOpExec op) {
        T in = stack.pop();
        T out = processor.process(op, in);
        stack.push(out);
        return out;
    }

    @Override
    public T visit(CmdOpPipeline op) {
        T in = stack.pop();
        T out = processor.process(op, in);
        stack.push(out);
        return out;
    }

    @Override
    public T visit(CmdOpGroup op) {
        T in = stack.pop();
        T out = processor.process(op, in);
        stack.push(out);
        return out;
    }

    @Override
    public T visit(CmdOpVar op) {
        T in = stack.pop();
        T out = processor.process(op, in);
        stack.push(out);
        return out;
    }
}