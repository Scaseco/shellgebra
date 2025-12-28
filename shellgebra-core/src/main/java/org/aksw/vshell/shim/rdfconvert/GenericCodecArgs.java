package org.aksw.vshell.shim.rdfconvert;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Unmatched;

public class GenericCodecArgs
    implements Args
{
    @Option(names = {"-d", "--decompress"}, description = "Decompress")
    boolean decode;

    @Option(names = {"-c", "--stdout"}, description = "Output to console.")
    boolean stdout;

    @Parameters(arity = "0..*", description = "File names")
    public List<String> fileNames = new ArrayList<>();

    public List<String> getFileNames() {
        return fileNames;
    }

    @Unmatched
    List<String> unmatchedArgs = new ArrayList<>();

    public boolean isDecode() {
        return decode;
    }

    public boolean isStdout() {
        return stdout;
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
        ArgumentList result = ArgumentListBuilder.newBuilder()
            .ifTrue(model.isDecode(), "-d")
            .ifTrue(model.isStdout(), "-c")
            .files(model.getFileNames())
            .args(model.getUnmatchedArgs())
            .build();
        return result;
    }

    public static Boolean stdinTest(GenericCodecArgs model) {
        return model.readsStdin().orElse(null);
    }

    public static ArgsModular<GenericCodecArgs> parse(String[] args) {
        GenericCodecArgs model = ArgsParserPicocli.of(GenericCodecArgs::new).parse(args);
        return new ArgsModular<>(model, GenericCodecArgs::renderArgList, GenericCodecArgs::stdinTest);
    }

    @Override
    public Optional<Boolean> readsStdin() {
        boolean result = fileNames.isEmpty() || fileNames.contains("-");
        return Optional.of(result);
    }
}
