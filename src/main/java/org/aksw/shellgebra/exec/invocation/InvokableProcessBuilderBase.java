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
        this.invocation = new Invocation.Argv(List.of(argv));
        return self();
    }

    // @Override
    public X script(String content, String mediaType) {
        this.invocation = new Invocation.Script(content, mediaType);
        return self();
    }

    @Override
    protected X cloneActual() {
        X result = super.clone();
        result.invocation(invocation());
        return result;
    }
}
