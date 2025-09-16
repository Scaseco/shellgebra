package org.aksw.vshell.shim.rdfconvert;

import java.util.ArrayList;
import java.util.List;

import picocli.CommandLine.Option;
import picocli.CommandLine.Unmatched;

public class GenericCodecArgs {
    @Option(names = {"-d", "--decompress"}, description = "Decompress")
    boolean decode;

//    @Option(names = {"-c", "--stdeout"}, description = "Stdout")
//    boolean stdout;

    @Unmatched
    List<String> unmatchedArgs = new ArrayList<>();

    public boolean isDecode() {
        return decode;
    }

    public List<String> getUnmatchedArgs() {
        return unmatchedArgs;
    }

    @Override
    public String toString() {
        return "GenericCodecArgs [decode=" + decode + ", unmatchedArgs=" + unmatchedArgs + "]";
    }

    public List<String> toArgLine() {
        List<String> result = CmdBuilder.newBuilder()
            .ifTrue(decode, "-d")
            .args(unmatchedArgs)
            .build();
        return result;
    }
}
