package org.aksw.vshell.shim.rdfconvert;

import java.util.ArrayList;
import java.util.List;

import picocli.CommandLine.Option;
import picocli.CommandLine.Unmatched;

public class GenericCodecArgs
    implements Args
{
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

    @Override
    public ArgumentList toArgList() {
        List<String> args = CmdBuilder.newBuilder()
            .ifTrue(decode, "-d")
            .args(unmatchedArgs)
            .build();
        return ArgumentList.ofLiterals(args);
    }
}
