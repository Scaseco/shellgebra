package org.aksw.shellgebra.processbuilder;

import java.util.List;

import org.aksw.vshell.registry.JvmCommandRegistry;
import org.aksw.vshell.registry.ProcessBuilderJvm;
import org.aksw.vshell.registry.ProcessBuilderNative;

/**
 * Simple DSL - Experimental.
 * Deprecated because use of the explicit ProcessBuilderX classes exhibits better clarity.
 */
@Deprecated
public class ProcessBuilders {

    /* --- Native --- */
    public static ProcessBuilderNative system(String ...command) {
        return ProcessBuilderNative.of(command);
    }

    /* --- Jvm --- */

    public static ProcessBuilderJvm jvm(JvmCommandRegistry jvmCmdRegistry, String ...argv) {
        return ProcessBuilderJvm.of(jvmCmdRegistry).command(argv);
    }

    public static ProcessBuilderJvm jvm(JvmCommandRegistry jvmCmdRegistry, List<String> argv) {
        return  ProcessBuilderJvm.of(jvmCmdRegistry).command(argv);
    }

    /* --- Pipeline --- */

    public static ProcessBuilderPipeline pipeline(IProcessBuilderCore<?> ... processBuilders) {
        return ProcessBuilderPipeline.of(processBuilders);
    }

    public static ProcessBuilderPipeline pipline(List<? extends IProcessBuilderCore<?>> processBuilders) {
        return ProcessBuilderPipeline.of(processBuilders);
    }

    /* --- Group --- */

    public static ProcessBuilderGroup group(IProcessBuilderCore<?> ... processBuilders) {
        return ProcessBuilderGroup.of(processBuilders);
    }

    public static ProcessBuilderGroup group(List<? extends IProcessBuilderCore<?>> processBuilders) {
        return ProcessBuilderGroup.of(processBuilders);
    }
}
