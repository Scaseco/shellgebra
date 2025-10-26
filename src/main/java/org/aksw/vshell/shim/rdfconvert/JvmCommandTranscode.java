package org.aksw.vshell.shim.rdfconvert;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import org.aksw.shellgebra.exec.Stage;
import org.aksw.shellgebra.exec.Stages;
import org.aksw.shellgebra.registry.codec.InputStreamTransformOverCommonsCompress;
import org.aksw.shellgebra.registry.codec.OutputStreamTransformOverCommonsCompress;
import org.aksw.shellgebra.unused.algebra.plan.InputStreamTransform;
import org.aksw.shellgebra.unused.algebra.plan.OutputStreamTransform;
import org.aksw.vshell.registry.JvmCommand;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

public class JvmCommandTranscode
    implements JvmCommand
{
    private String codecName;
    private InputStreamTransform inTransform;
    private OutputStreamTransform outTransform;

    public JvmCommandTranscode(String codecName, InputStreamTransform inTransform, OutputStreamTransform outTransform) {
        super();
        this.codecName = codecName;
        this.inTransform = inTransform;
        this.outTransform = outTransform;
    }

    @Override
    public Stage newStage(String... args) {
        ArgsModular<GenericCodecArgs> transcodeModel = GenericCodecArgs.parse(args);
        GenericCodecArgs model = transcodeModel.model();
        Stage result;
        if (model.isDecode()) {
            Objects.requireNonNull(inTransform, "No decoding for " + codecName);
            result = Stages.javaIn(inTransform);
        } else {
            Objects.requireNonNull(inTransform, "No encoding for " + codecName);
            result = Stages.javaOut(outTransform);
        }
        return result;
    }

    public static JvmCommandTranscode of(CompressorStreamFactory provider, String codecName) {
        Objects.requireNonNull(provider);
        Objects.requireNonNull(codecName);
        Set<String> inNames = provider.getInputStreamCompressorNames();
        InputStreamTransform inTransform = inNames.contains(codecName)
            ? new InputStreamTransformOverCommonsCompress(provider, codecName)
            : null;


        Set<String> outNames = provider.getOutputStreamCompressorNames();
        OutputStreamTransform outTransform = outNames.contains(codecName)
            ? new OutputStreamTransformOverCommonsCompress(provider, codecName)
            : null;

        if (inTransform == null && outTransform == null) {
            throw new NoSuchElementException("No transcoding with name: " + codecName);
        }

        return new JvmCommandTranscode(codecName, inTransform, outTransform);
    }
}
