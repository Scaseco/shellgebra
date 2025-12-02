package org.aksw.vshell.shim.rdfconvert;

import java.io.IOException;

import org.aksw.shellgebra.algebra.stream.transform.StreamingRDFConverter;
import org.aksw.shellgebra.exec.Stage;
import org.aksw.shellgebra.exec.StageJvm;
import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.aksw.shellgebra.unused.algebra.plan.InputStreamTransform;

public class JvmCommandRapper
    extends JvmCommandBase<RapperArgs>
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
        return new StageJvm(transform);
    }

    @Override
    public void runActual(ProcessRunner cxt, RapperArgs model) throws IOException {
        InputStreamTransform transform = StreamingRDFConverter.converter(
                model.getInputFormat(), model.getOutputFormat(), model.getBaseUrl());
        transform.apply(cxt.getInputStream()).transferTo(cxt.getOutputStream());
    }
}
