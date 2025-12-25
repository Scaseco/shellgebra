package org.aksw.vshell.shim.rdfconvert;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import org.aksw.shellgebra.registry.codec.InputStreamTransformOverCommonsCompress;
import org.aksw.shellgebra.registry.codec.OutputStreamTransformOverCommonsCompress;
import org.aksw.shellgebra.unused.algebra.plan.InputStreamTransform;
import org.aksw.shellgebra.unused.algebra.plan.OutputStreamTransform;
import org.aksw.vshell.registry.JvmExecCxt;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

public class JvmCommandTranscode
    extends JvmCommandBase<GenericCodecArgs>
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
        ArgsModular<GenericCodecArgs> result = GenericCodecArgs.parse(args);
        return result;
    }

    @Override
    protected void runActual(JvmExecCxt cxt, GenericCodecArgs model) throws IOException {
        System.out.println("transcode called");
        if (model.isDecode()) {
            Objects.requireNonNull(inTransform, "No decoding for " + codecName);
            InputStream encodedIn = cxt.in().inputStream();
            InputStream decodedIn = inTransform.apply(encodedIn);
            OutputStream os = cxt.out().outputStream();
            System.out.println("transcode reading started.");
            decodedIn.transferTo(os);
            if (false) {
                int c;
                while ((c = decodedIn.read()) != -1) {
                    System.out.println("Read byte: " + c);
                }
            }
            System.out.println("transcode terminated");
        } else {
            Objects.requireNonNull(inTransform, "No encoding for " + codecName);
            InputStream in = cxt.in().inputStream();
            OutputStream rawOut = cxt.out().outputStream();
            OutputStream out = outTransform.apply(rawOut);
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

    //
//  @Override
//  public Stage newStage(String... args) {
//      GenericCodecArgs model = parseArgs(args).model();
//      Stage result;
//      if (model.isDecode()) {
//          Objects.requireNonNull(inTransform, "No decoding for " + codecName);
//          result = Stages.javaIn(inTransform);
//      } else {
//          Objects.requireNonNull(inTransform, "No encoding for " + codecName);
//          result = Stages.javaOut(outTransform);
//      }
//      return result;
//  }
}
