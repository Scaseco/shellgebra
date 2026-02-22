package org.aksw.shellgebra.shim.codec;

import java.util.List;

import org.aksw.shellgebra.shim.cmd.GenericCodecArgs;
import org.aksw.shellgebra.shim.core.ArgsTransform;
import org.aksw.shellgebra.shim.picocli.ArgsParserPicocli;

public class ArgsTransformTranscode
    implements ArgsTransform
{
    @Override
    public List<String> map(List<String> args) {
        // Try to parse the args using the GenericCodecArgs model.
        GenericCodecArgs model = ArgsParserPicocli.of(GenericCodecArgs::new).parse(args);
        return args;
    }
}
