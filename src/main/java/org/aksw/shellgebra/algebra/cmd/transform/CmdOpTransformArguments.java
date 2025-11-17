package org.aksw.shellgebra.algebra.cmd.transform;

import java.util.List;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgPath;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgString;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.transformer.CmdOpTransformBase;
import org.aksw.shellgebra.exec.SysRuntime;

public class CmdOpTransformArguments
    implements CmdOpTransformBase
{
    protected SysRuntime runtime;

    public CmdOpTransformArguments(SysRuntime runtime) {
        super();
        this.runtime = runtime;
    }

    @Override
    public CmdOp transform(CmdOpExec op, List<CmdArg> args) {
        CmdOp result;
        if (args.stream().anyMatch(x -> x instanceof CmdArgPath)) {
            List<CmdArg> newArgs = args.stream().map(this::handleFile).toList();
            result = new CmdOpExec(op.getName(), newArgs, op.redirects());
        } else {
            result = CmdOpTransformBase.super.transform(op, args);
        }
        return result;
    }

    protected CmdArg handleFile(CmdArg arg) {
        CmdArg result = arg instanceof CmdArgPath argPath
            ? new CmdArgString(runtime.quoteFileArgument(argPath.path()))
            : arg;
        return result;
    }

}
