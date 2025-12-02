package org.aksw.vshell.shim.rdfconvert;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import org.aksw.shellgebra.exec.Stage;
import org.aksw.shellgebra.exec.Stages;
import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.aksw.shellgebra.registry.codec.InputStreamTransformOverCommonsCompress;
import org.aksw.shellgebra.registry.codec.OutputStreamTransformOverCommonsCompress;
import org.aksw.shellgebra.unused.algebra.plan.InputStreamTransform;
import org.aksw.shellgebra.unused.algebra.plan.OutputStreamTransform;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

public class JvmCommandTranscode
    extends JvmCommandBase<ArgsModular<GenericCodecArgs>>
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
    public ArgsModular<GenericCodecArgs> parseArgs(String... args) {
        ArgsModular<GenericCodecArgs> argsModel = GenericCodecArgs.parse(args);
        return argsModel;
    }

    @Override
    public Stage newStage(String... args) {
        GenericCodecArgs model = parseArgs(args).model();
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

    @Override
    protected void runActual(ProcessRunner cxt, ArgsModular<GenericCodecArgs> args) throws IOException {
        GenericCodecArgs model = args.model();
        if (model.isDecode()) {
            Objects.requireNonNull(inTransform, "No decoding for " + codecName);
            InputStream in = cxt.internalIn();
            InputStream out = inTransform.apply(in);
            out.transferTo(cxt.internalOut());
        } else {
            Objects.requireNonNull(inTransform, "No encoding for " + codecName);
            InputStream in = cxt.internalIn();
            OutputStream out = outTransform.apply(cxt.internalOut());
            in.transferTo(out);
        }
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
