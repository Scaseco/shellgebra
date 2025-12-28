package org.aksw.shellgebra.algebra.stream.transform;

//public class StreamOpTransformToCmdPlacement
//    extends StreamOpTransformBase
//{
//    private CodecRegistry registry;
//    private CodecSysEnv env;
//
//    public StreamOpTransformToCmdPlacement(CodecRegistry registry, CodecSysEnv env) {
//        super();
//        this.registry = registry;
//        this.env = env;
//    }
//
//    @Override
//    public StreamOp transform(StreamOpFile op) {
//        return new StreamOpCommand(new CmdOpFile(op.getPath()));
//     }
//
//    @Override
//    public StreamOp transform(StreamOpConcat op, List<StreamOp> subOps) {
//        boolean isPushable = subOps.stream().allMatch(x -> x instanceof StreamOpCommand);
//
//        StreamOp result;
//        if (isPushable) {
//            List<CmdOp> args = subOps.stream().map(x -> (StreamOpCommand)x).map(StreamOpCommand::getCmdOp).toList();
//            result = new StreamOpCommand(new CmdOpGroup(args));
//        } else {
//            result = super.transform(op, subOps);
//        }
//        return result;
//    }
//
//    public static String resolveCmdName(String toolName, SysRuntime runtime) {
//    //    String[] cmd = codecVariant.getCmd();
//    //    if (cmd.length == 0) {
//    //        throw new IllegalStateException("Encountered zero-length command");
//    //    }
//        String rawCmdName = toolName; //cmd[0];
//        String resolvedCmdName;
//        try {
//            resolvedCmdName = runtime.which(rawCmdName);
//        } catch (IOException | InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        return resolvedCmdName;
//    }
//
//    @Override
//    public StreamOp transform(StreamOpTranscode op, StreamOp subOp) {
//        String name = op.getName();
//        StreamOp result = null;
//
//        CodecSpec spec = registry.getCodecSpec(name)
//            .orElseThrow(() -> new NoSuchElementException("No codec with name: " + name));
//        for (CodecVariant variant : spec.getDecoderVariants()) {
//            // String[] cmd = variant.getCmd();
//            String toolName = variant.getToolName();
//            String resolvedCmdName = resolveCmdName(toolName, env.getRuntime());
//
//            // cmd[0] = resolvedCmdName;
//            List<CmdOp> args = new ArrayList<>();
//            variant.getArgs().forEach(s -> args.add(new CmdOpString(s)));
//            SysRuntime runtime = env.getRuntime();
//
//            boolean canSubst = true;
//            boolean supportsStdIn = true;
//            boolean supportsFile = true;
//
//            if (subOp instanceof StreamOpCommand subCmd) {
//                CmdOp newCmdOp;
//                CmdOp cmdOp = subCmd.getCmdOp();
//
//                if (supportsFile && cmdOp instanceof CmdOpFile fileOp) {
//                    args.add(new CmdOpFile(fileOp.getPath()));
//                    newCmdOp = new CmdOpExec(resolvedCmdName, args);
//                } else if (supportsStdIn) {
//                    newCmdOp = new CmdOpExec(resolvedCmdName, args);
//                    newCmdOp = new CmdOpPipe(cmdOp, newCmdOp);
//                } else {
//                    // String[] parts = runtime.compileCommand(cmdOp);
//                    // CmdOp subC = new CmdOpSubst(CmdOpExec.of(parts));
//                    args.add(new CmdOpSubst(cmdOp));
//                    newCmdOp = new CmdOpExec(resolvedCmdName, args);
//                }
//                result = new StreamOpCommand(newCmdOp);
//            } if (subOp instanceof StreamOpFile cmdOfFile) {
//                result = new StreamOpCommand(CmdOpExec.ofLiterals("cat", cmdOfFile.getPath()));
//            }
//
//            // Accept the first result
//            if (result != null) {
//                break;
//            }
//        }
//
//        // If no codec found then just to the default transform.
//        if (result == null) {
//            result = super.transform(op, subOp);
//        }
//        return result;
//    }
//}
