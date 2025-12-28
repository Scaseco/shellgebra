package org.aksw.vshell.shim.rdfconvert;

import java.util.ArrayList;
import java.util.List;

import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Unmatched;

/**
 * Picocli model that mimics rapper args.
 */
public class RapperArgs {
    @Option(names = "-i", description = "Input format")
    String inputFormat;

    @Option(names = "-o", description = "Output format")
    String outputFormat;

    @Parameters(index = "0", arity = "0..1", description = "Input file (use '-' for stdin)")
    String inputFile;

    @Parameters(index = "1", arity = "0..1", description = "Base URL")
    String baseUrl;

    @Unmatched
    List<String> unmatchedArgs = new ArrayList<>();

    public String getInputFormat() {
        return inputFormat;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public String getInputFile() {
        return inputFile;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public List<String> getUnmatchedArgs() {
        return unmatchedArgs;
    }

//    @Override
//    public ArgumentList toArgList() {
//        return renderArgList(this);
//    }

    public static ArgumentList renderArgList(RapperArgs model) {
        ArgumentList result = ArgumentListBuilder.newBuilder()
            .opt("-i", model.getInputFormat())
            .opt("-o", model.getOutputFormat())
            .fileOrLiteral(model.getInputFile(), "-")
            .arg(model.getBaseUrl())
            .args(model.getUnmatchedArgs())
            .build();
        return result;
    }

    public static ArgsModular<RapperArgs> parse(String[] args) {
        RapperArgs model = ArgsParserPicocli.of(RapperArgs::new).parse(args);
        return new ArgsModular<>(model, RapperArgs::renderArgList, a -> a.getInputFile().equals("-"));
    }

    @Override
    public String toString() {
        return "inputFormat=" + inputFormat + ", outputFormat=" + outputFormat + ", inputFile=" + inputFile
                + ", baseUrl=" + baseUrl + ", unmatchedArgs=" + unmatchedArgs ;
    }
}
