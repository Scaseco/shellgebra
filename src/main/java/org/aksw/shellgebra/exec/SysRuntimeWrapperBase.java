package org.aksw.shellgebra.exec;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.transform.CmdString;

// Not used
public class SysRuntimeWrapperBase<X extends SysRuntime>
    implements SysRuntimeWrapper<X>
{
    protected X delegate;

    public SysRuntimeWrapperBase(X delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public X getDelegate() {
        return delegate;
    }

    @Override
    public CmdString compileString(CmdOp op) {
        // TODO Auto-generated method stub
        return null;
    }
}
