package org.aksw.shellgebra.shim.cmd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import org.aksw.commons.io.util.stream.InputStreamTransform;
import org.aksw.commons.io.util.stream.OutputStreamTransform;
import org.aksw.shellgebra.shim.core.ArgsModular;
import org.aksw.vshell.registry.JvmExecCxt;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JvmCommandTranscode
    extends JvmCommandBase<GenericCodecArgs>
{
    private String codecName;
    private InputStreamTransform inTransform;
    private OutputStreamTransform outTransform;

    private static final Logger logger = LoggerFactory.getLogger(JvmCommandTranscode.class);

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
        logger.debug("transcode called");
        if (model.isDecode()) {
            Objects.requireNonNull(inTransform, "No decoding for " + codecName);
            InputStream encodedIn = cxt.in().inputStream();

            // logger.debug("CONSUMED INPUT DATA WAS: {}", IOUtils.toString(encodedIn, StandardCharsets.UTF_8));

            InputStream decodedIn = inTransform.apply(encodedIn);
            OutputStream os = cxt.out().outputStream();
            logger.debug("transcode reading started.");
            decodedIn.transferTo(os);
            if (false) {
                int c;
                while ((c = decodedIn.read()) != -1) {
                    logger.debug("Read byte: {}", c);
                }
            }
            logger.debug("transcode terminated");
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
