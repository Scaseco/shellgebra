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
        return renderArgList(this);
    }

    public static ArgumentList renderArgList(GenericCodecArgs model) {
        ArgumentList result = CmdBuilder.newBuilder()
            .ifTrue(model.isDecode(), "-d")
            .args(model.getUnmatchedArgs())
            .build();
        return result;
    }

    public static ArgsModular<GenericCodecArgs> parse(String[] args) {
        GenericCodecArgs model = ArgsParserPicocli.of(GenericCodecArgs::new).parse(args);
        return new ArgsModular<>(model, GenericCodecArgs::renderArgList);
    }
}
