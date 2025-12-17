package org.aksw.shellgebra.exec.invocation;

import java.util.List;

import org.aksw.shellgebra.exec.ProcessBuilderCore;

public abstract class InvokableProcessBuilderBase<X extends InvokableProcessBuilderBase<X>>
    extends ProcessBuilderCore<X>
{
    private Invocation invocation;

    public X invocation(Invocation invocation) {
        this.invocation = invocation;
        return self();
    }

    public Invocation invocation() {
        return invocation;
    }

    public X command(String... argv) {
        command(List.of(argv));
        return self();
    }

    public X command(List<String> argv) {
        invocation(new Invocation.Argv(argv));
        return self();
    }

    // @Override
    public X script(String content, String mediaType) {
        invocation(new Invocation.Script(content, mediaType));
        return self();
    }

    @Override
    public X clone() {
        X result = super.clone();
        result.invocation(invocation());
        return result;
    }
}
