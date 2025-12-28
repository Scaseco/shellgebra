package org.aksw.shellgebra.algebra.cmd.transform;

//public class CmdOpTransformArguments
//    implements CmdOpTransformBase
//{
//    protected SysRuntime runtime;
//
//    public CmdOpTransformArguments(SysRuntime runtime) {
//        super();
//        this.runtime = runtime;
//    }
//
//    @Override
//    public CmdOp transform(CmdOpExec op, List<CmdArg> args) {
//        CmdOp result;
//        if (args.stream().anyMatch(x -> x instanceof CmdArgPath)) {
//            List<CmdArg> newArgs = args.stream().map(this::handleFile).toList();
//            result = new CmdOpExec(op.getName(), newArgs, op.redirects());
//        } else {
//            result = CmdOpTransformBase.super.transform(op, args);
//        }
//        return result;
//    }
//
//    protected CmdArg handleFile(CmdArg arg) {
//        CmdArg result = arg instanceof CmdArgPath argPath
//            ? new CmdArgString(runtime.quoteFileArgument(argPath.path()))
//            : arg;
//        return result;
//    }
//
//}
