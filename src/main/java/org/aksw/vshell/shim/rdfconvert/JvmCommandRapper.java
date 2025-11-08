package org.aksw.vshell.shim.rdfconvert;

import org.aksw.shellgebra.algebra.stream.transform.StreamingRDFConverter;
import org.aksw.shellgebra.exec.JvmStage;
import org.aksw.shellgebra.exec.Stage;
import org.aksw.shellgebra.unused.algebra.plan.InputStreamTransform;
import org.aksw.vshell.registry.JvmCommand;

public class JvmCommandRapper
    implements JvmCommand
{
    @Override
    public RapperArgs parseArgs(String... args) {
        ArgsModular<RapperArgs> rapperModel = RapperArgs.parse(args);
        RapperArgs model = rapperModel.model();
        return model;
    }

    @Override
    public Stage newStage(String... args) {
        RapperArgs model = parseArgs(args);
        InputStreamTransform transform = StreamingRDFConverter.converter(
                model.getInputFormat(), model.getOutputFormat(), model.getBaseUrl());
        return new JvmStage(transform);
    }
}
