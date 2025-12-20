package org.aksw.shellgebra.algebra.stream.transform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgCmdOp;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgLiteral;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.stream.op.CodecSpec;
import org.aksw.shellgebra.algebra.stream.op.CodecSysEnv;
import org.aksw.shellgebra.algebra.stream.op.StreamOp;
import org.aksw.shellgebra.algebra.stream.op.StreamOpCommand;
import org.aksw.shellgebra.algebra.stream.op.StreamOpConcat;
import org.aksw.shellgebra.algebra.stream.op.StreamOpFile;
import org.aksw.shellgebra.algebra.stream.op.StreamOpTranscode;
import org.aksw.shellgebra.algebra.stream.transformer.StreamOpTransformBase;
import org.aksw.shellgebra.exec.SysRuntime;
import org.aksw.shellgebra.registry.codec.CodecRegistry;
import org.aksw.shellgebra.registry.codec.CodecVariant;

// CmdOp transformation that does not consider execution sites such as host or docker container.
// FIXME StreamOps (if even needed at all) should only translate to virtual commands.
public class StreamOpTransformToCmdOp
    extends StreamOpTransformBase
{
    private CodecRegistry registry;
    private CodecSysEnv env;

    public StreamOpTransformToCmdOp(CodecRegistry registry, CodecSysEnv env) {
        super();
        this.registry = registry;
        this.env = env;
    }

    @Override
    public StreamOp transform(StreamOpFile op) {
        throw new UnsupportedOperationException("Cannot tranform file; must be a child of another stream op.");
        // return new StreamOpCommand(new CmdOpFile(op.getPath()));
     }

    @Override
    public StreamOp transform(StreamOpConcat op, List<StreamOp> subOps) {
        boolean isPushable = subOps.stream().allMatch(x -> x instanceof StreamOpCommand);

        StreamOp result;
        if (isPushable) {
            List<CmdOp> args = subOps.stream().map(x -> (StreamOpCommand)x).map(StreamOpCommand::getCmdOp).toList();
            result = new StreamOpCommand(new CmdOpGroup(args, List.of()));
        } else {
            result = super.transform(op, subOps);
        }
        return result;
    }

    public static String resolveCmdName(String toolName, SysRuntime runtime) {
//        String[] cmd = codecVariant.getCmd();
//        if (cmd.length == 0) {
//            throw new IllegalStateException("Encountered zero-length command");
//        }
        String rawCmdName = toolName; //cmd[0];
        String resolvedCmdName;
        try {
            resolvedCmdName = runtime.which(rawCmdName);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return resolvedCmdName;
    }

    @Override
    public StreamOp transform(StreamOpTranscode op, StreamOp subOp) {
        String name = op.getName();
        StreamOp result = null;

        CodecSpec spec = registry.getCodecSpec(name)
            .orElseThrow(() -> new NoSuchElementException("No codec with name: " + name));
        for (CodecVariant variant : spec.getDecoderVariants()) {
            // String[] cmd = variant.getCmd();
            String toolName = variant.getToolName();
            String resolvedCmdName = resolveCmdName(toolName, env.getRuntime());

            // cmd[0] = resolvedCmdName;
            List<CmdArg> args = new ArrayList<>();
            variant.getArgs().forEach(s -> args.add(new CmdArgLiteral(s)));
            SysRuntime runtime = env.getRuntime();

            boolean canSubst = true;
            boolean supportsStdIn = true;
            boolean supportsFile = true;

            if (subOp instanceof StreamOpCommand subCmd) {
                CmdOp newCmdOp;
                CmdOp cmdOp = subCmd.getCmdOp();

                if (false) { //supportsFile && cmdOp instanceof CmdOpFile fileOp) {
                    // args.add(CmdArg.ofPath(fileOp.getPath()));
                    newCmdOp = CmdOpExec.of(resolvedCmdName, args);
                } else if (supportsStdIn) {
                    newCmdOp = CmdOpExec.of(resolvedCmdName, args);
                    newCmdOp = CmdOpPipeline.of(cmdOp, newCmdOp);
                } else {
                    // String[] parts = runtime.compileCommand(cmdOp);
                    // CmdOp subC = new CmdOpSubst(CmdOpExec.of(parts));
                    args.add(new CmdArgCmdOp(cmdOp));
                    newCmdOp = CmdOpExec.of(resolvedCmdName, args);
                }
                result = new StreamOpCommand(newCmdOp);
            } if (subOp instanceof StreamOpFile cmdOfFile) {
                result = new StreamOpCommand(CmdOpExec.ofLiterals("cat", cmdOfFile.getPath()));
            }

            // Accept the first result
            if (result != null) {
                break;
            }
        }

        // If no codec found then just to the default transform.
        if (result == null) {
            result = super.transform(op, subOp);
        }
        return result;
    }
}
