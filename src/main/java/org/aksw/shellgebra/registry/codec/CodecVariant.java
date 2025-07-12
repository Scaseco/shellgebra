package org.aksw.shellgebra.registry.codec;

import java.util.List;

import org.aksw.shellgebra.algebra.common.TranscodeMode;

// Variants: Different commands / paths but same arguments.
public class CodecVariant {
    protected String toolName;

    /** Encoding or decoding */
    protected TranscodeMode mode;
    protected List<String> args;

    private CodecVariant(String toolName, List<String> args) {
        super();
        this.toolName = toolName;
        this.args = args;
    }

    public static CodecVariant of(String toolName, String ...args) {
        return new CodecVariant(toolName, List.of(args));
    }

    public String getToolName() {
        return toolName;
    }

    public List<String> getArgs() {
        return args;
    }

//    CodecOpCommand createCmdFromFile(String file) {
//        return new CodecOpCommand(new String[] {"cat", file});
//    }

//    CodecOp createCmdFromSubCmd(SysRuntime env, String[] cmd) {
//        String  env.processSubstitute(cmd);
//        // return null;
//    }

}
