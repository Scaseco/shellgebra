package org.aksw.vshell.shim.rdfconvert;

import org.aksw.shellgebra.algebra.stream.transform.StreamingRDFConverter;
import org.aksw.shellgebra.exec.JvmStage;
import org.aksw.shellgebra.exec.Stage;
import org.aksw.shellgebra.unused.algebra.plan.InputStreamTransform;

public class JvmCommandRapper
    implements JvmCommand
{
    @Override
    public Stage newStage(String... args) {
        ArgsModular<RapperArgs> rapperModel = RapperArgs.parse(args);
        RapperArgs model = rapperModel.model();
        InputStreamTransform transform = StreamingRDFConverter.converter(
                model.getInputFormat(), model.getOutputFormat(), model.getBaseUrl());
        return new JvmStage(transform);
    }
}
