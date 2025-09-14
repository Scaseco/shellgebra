package org.aksw.vshell.shim.rdfconvert;

import java.util.ArrayList;
import java.util.List;

import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Unmatched;

/** Picocli model that mimicks rapper. */
public class RapperArgs {
    @Option(names = "-i", description = "Input format")
    String inputFormat;

    @Option(names = "-o", description = "Output format")
    String outputFormat;

    @Parameters(index = "0", description = "Input file (use '-' for stdin)")
    String inputFile;

    @Parameters(index = "1", description = "Base URL")
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

    @Override
    public String toString() {
        return "RapperArgs [inputFormat=" + inputFormat + ", outputFormat=" + outputFormat + ", inputFile=" + inputFile
                + ", baseUrl=" + baseUrl + ", unmatchedArgs=" + unmatchedArgs + "]";
    }
}
