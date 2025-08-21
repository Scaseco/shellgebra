package org.aksw.shellgebra.algebra.stream.op;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.shellgebra.algebra.common.OpSpecContentConvert;
import org.aksw.shellgebra.algebra.common.OpSpecTranscoding;
import org.aksw.shellgebra.algebra.common.TranscodeMode;
import org.aksw.shellgebra.algebra.stream.transform.StreamOpTransformToCmdOp;
import org.aksw.shellgebra.algebra.stream.transformer.StreamOpTransform;
import org.aksw.shellgebra.exec.SysRuntime;
import org.aksw.shellgebra.exec.SysRuntimeImpl;
import org.aksw.shellgebra.registry.codec.CodecRegistry;
import org.aksw.shellgebra.registry.codec.CodecVariant;
import org.aksw.shellgebra.registry.codec.JavaStreamTransform;
import org.aksw.shellgebra.registry.content.ContentConvertRegistry;
import org.aksw.shellgebra.registry.content.Tool;
import org.aksw.shellgebra.registry.tool.ToolInfo;
import org.aksw.shellgebra.registry.tool.ToolRegistry;

/**
 * FIXME Should the outcome be the actually available commands or just a subset of the registry
 * for further processing?
 */
public class StreamOpTransformResolve
    implements StreamOpTransform
{
    protected SysRuntime sysRuntime;
    protected CodecRegistry codecRegistry;
    protected ContentConvertRegistry convertRegistry;
    protected ToolRegistry toolRegistry;

    public StreamOpTransformResolve() {
        this(SysRuntimeImpl.forCurrentOs(), CodecRegistry.get(), ContentConvertRegistry.get(), ToolRegistry.get());
    }

    public StreamOpTransformResolve(
            SysRuntime sysRuntime,
            CodecRegistry codecRegistry,
            ContentConvertRegistry convertRegistry,
            ToolRegistry toolRegistry) {
        super();
        this.sysRuntime = sysRuntime;
        this.codecRegistry = codecRegistry;
        this.convertRegistry = convertRegistry;
        this.toolRegistry = toolRegistry;
    }

    @Override
    public StreamOp transform(StreamOpTranscode op, StreamOp subOp) {
        OpSpecTranscoding transcoding = op.getTranscoding();
        Resolution1 resolution = processTranscode(transcoding);
        StreamOpResolution result = new StreamOpResolution(resolution, subOp);
        return result;
    }

    public Resolution1 processTranscode(OpSpecTranscoding transcoding) {
        Resolution1 resolution = new Resolution1();
        String name = transcoding.name();

        List<CodecVariant> variants = transcoding.mode().equals(TranscodeMode.DECODE)
            ? codecRegistry.getDecoders(name)
            : codecRegistry.getEncoders(name);

        for (CodecVariant variant : variants) {
            String toolName = variant.getToolName();
            String resolvedCmdName = StreamOpTransformToCmdOp.resolveCmdName(toolName, sysRuntime);

            ToolInfo baseToolInfo = toolRegistry.getToolInfo(toolName).orElse(null);
            if (baseToolInfo != null) {
                resolution.getTools().merge(baseToolInfo);
            }

            // ToolInfo resolvedToolInfo = new ToolInfo(toolName);
            if (resolvedCmdName != null) {
                ToolInfo resolvedToolInfo = resolution.getTools().getOrCreate(toolName);
                resolvedToolInfo.getOrCreateCommand(resolvedCmdName);
                resolution.getTools().merge(resolvedToolInfo);
            }
        }

        JavaStreamTransform javaCodec = codecRegistry.getJavaCodec(transcoding).orElse(null);

        if (javaCodec != null) {
            resolution.setInputStreamTransform(javaCodec.inputStreamTransform());
            resolution.setOutputStreamTransform(javaCodec.outputStreamTransform());
        }

        return resolution;
    }

    @Override
    public StreamOp transform(StreamOpContentConvert op, StreamOp subOp) {
        OpSpecContentConvert spec = op.getContentConvertSpec();
        Resolution1 resolution = processContentConvert(spec);
        StreamOpResolution result = new StreamOpResolution(resolution, subOp);
        return result;
    }

    public Resolution1 processOpSpec(Object opSpec) {
        Resolution1 result = opSpec instanceof OpSpecTranscoding x
            ? processTranscode(x)
            : opSpec instanceof OpSpecContentConvert y
                ? processContentConvert(y)
                : null;
        return result;
    }

    public Resolution1 processContentConvert(OpSpecContentConvert spec) {
        Resolution1 resolution = new Resolution1();

        JavaStreamTransform converter = convertRegistry.getJavaConverter(spec).orElse(null);
        if (converter != null) {
            resolution.setInputStreamTransform(converter.inputStreamTransform());
            resolution.setOutputStreamTransform(converter.outputStreamTransform());
        }

        // Find tools registered for the conversion
        List<Tool> tools = convertRegistry.getCmdConverter(spec);

        // Find resolution of the tools to commands (on host or in container)
        List<Entry<Tool, ToolInfo>> toolInfos = tools.stream()
            .flatMap(tool -> toolRegistry.getToolInfo(tool.name()).map(info -> Map.entry(tool, info)).stream())
            .toList()
            ;

        toolInfos.forEach(e -> resolution.getTools().merge(e.getValue()));

        return resolution;
    }

    public static String resolveCmdName(String toolName, SysRuntime runtime) {
//      String[] cmd = codecVariant.getCmd();
//      if (cmd.length == 0) {
//          throw new IllegalStateException("Encountered zero-length command");
//      }
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
    public StreamOp transform(StreamOpFile op) {
        return op;
    }

    @Override
    public StreamOp transform(StreamOpConcat op, List<StreamOp> subOps) {
        return new StreamOpConcat(subOps);
    }

    @Override
    public StreamOp transform(StreamOpCommand op) {
        return op;
    }

    @Override
    public StreamOp transform(StreamOpVar op) {
        return op;
    }

    @Override
    public StreamOp transform(StreamOpResolution op, StreamOp subOp) {
        return new StreamOpResolution(op.getResolution(), subOp);
    }

//
//  @Override
//  public StreamOp transform(StreamOpTranscode op, StreamOp subOp) {
//      String name = op.getName();
//      StreamOp result = null;
//
//      CodecSpec spec = registry.getCodecSpec(name)
//          .orElseThrow(() -> new NoSuchElementException("No codec with name: " + name));
//      for (CodecVariant variant : spec.getDecoderVariants()) {
//          // String[] cmd = variant.getCmd();
//          String toolName = variant.getToolName();
//          String resolvedCmdName = resolveCmdName(toolName, env.getRuntime());
//
//          // cmd[0] = resolvedCmdName;
//          List<CmdOp> args = new ArrayList<>();
//          variant.getArgs().forEach(s -> args.add(new CmdOpString(s)));
//          SysRuntime runtime = env.getRuntime();
//
//          boolean canSubst = true;
//          boolean supportsStdIn = true;
//          boolean supportsFile = true;
//
//          if (subOp instanceof StreamOpCommand subCmd) {
//              CmdOp newCmdOp;
//              CmdOp cmdOp = subCmd.getCmdOp();
//
//              if (supportsFile && cmdOp instanceof CmdOpFile fileOp) {
//                  args.add(new CmdOpFile(fileOp.getPath()));
//                  newCmdOp = new CmdOpExec(resolvedCmdName, args);
//              } else if (supportsStdIn) {
//                  newCmdOp = new CmdOpExec(resolvedCmdName, args);
//                  newCmdOp = new CmdOpPipe(cmdOp, newCmdOp);
//              } else {
//                  // String[] parts = runtime.compileCommand(cmdOp);
//                  // CmdOp subC = new CmdOpSubst(CmdOpExec.of(parts));
//                  args.add(new CmdOpSubst(cmdOp));
//                  newCmdOp = new CmdOpExec(resolvedCmdName, args);
//              }
//              result = new StreamOpCommand(newCmdOp);
//          } if (subOp instanceof StreamOpFile cmdOfFile) {
//              result = new StreamOpCommand(CmdOpExec.ofStrings("cat", cmdOfFile.getPath()));
//          }
//
//          // Accept the first result
//          if (result != null) {
//              break;
//          }
//      }
//
//      // If no codec found then just to the default transform.
//      if (result == null) {
//          result = super.transform(op, subOp);
//      }
//      return result;
//  }
//

//
//    protected boolean isSupported(StreamOpTranscode op) {
//        boolean result;
//        try {
//            StreamOp testOp = StreamOpTransformer.transform(op, sysCallTransform);
//            result = testOp instanceof StreamOpCommand;
//        } catch (Exception e) {
//            // XXX Should check what exception we are getting
//            result = false;
//        }
//        return result;
//    }

//  protected StreamOpEntry<Location> injectVar(StreamOp thisOp) {
//  StreamOpEntry<Location> result;
//  String varName = "v" + (nextVar++);
//  varToOp.put(varName, thisOp);
//  StreamOpVar v = new StreamOpVar(varName);
//  result = new StreamOpEntry<>(v, Location.NOT_HANDLED);
//  return result;
//}
//
//@Override
//public StreamOpEntry<Location> transform(StreamOpFile op) {
//  return new StreamOpEntry<>(op, Location.HANDLED);
//}
//
//@Override
//public StreamOpEntry<Location> transform(StreamOpVar op) {
//  throw new UnsupportedOperationException();
//}
//
//@Override
//public StreamOpEntry<Location> transform(StreamOpCommand op) {
//  throw new UnsupportedOperationException();
//}
//
//@Override
//public StreamOpEntry<Location> transform(StreamOpConcat op, List<StreamOpEntry<Location>> subOps) {
//  throw new UnsupportedOperationException();
//}


//  StreamOpEntry<Location> result = null;
//
//  if (subOp.getValue() == Location.HANDLED) {
//      boolean isSupported = false;
//      if (!isSupported) {
//          StreamOp thisOp = new StreamOpContentConvert(op.getSourceFormat(), op.getTargetFormat(), op.getBaseIri(), subOp.getStreamOp());
//          result = injectVar(thisOp);
//      }
//  }
//
//  if (result == null) {
//      // StreamOp newOp = super.transform(op, subOp.getStreamOp());
//      // StreamOpTranscode newOp = new StreamOpTranscode(op.getTranscoding(), subOp.getStreamOp());
//      StreamOpContentConvert newOp = new StreamOpContentConvert(op.getSourceFormat(), op.getTargetFormat(), op.getBaseIri(), subOp.getStreamOp());
//      result = new StreamOpEntry<>(newOp, subOp.getValue());
//  }
}
