package org.aksw.vshell.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.aksw.vshell.shim.rdfconvert.Args;
import org.aksw.vshell.shim.rdfconvert.ArgsModular;
import org.aksw.vshell.shim.rdfconvert.ArgsParserPicocli;
import org.aksw.vshell.shim.rdfconvert.ArgumentList;
import org.aksw.vshell.shim.rdfconvert.ArgumentListBuilder;

import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

public class ArgsHead
    implements Args
{

    // TODO Add support for '-' prefix
    @Option(names = {"-n", "--lines"}, description = "Lines")
    private Long lines = null;

    @Parameters(arity = "0..*", description = "File names")
    public List<String> fileNames = new ArrayList<>();

    public List<String> getFileNames() {
        return fileNames;
    }

    public Optional<Long> getLines() {
        return Optional.ofNullable(lines);
    }

    @Override
    public ArgumentList toArgList() {
        return renderArgList(this);
    }

    @Override
    public String toString() {
        return "ArgsCat [fileNames=" + fileNames + "]";
    }

    public static ArgumentList renderArgList(ArgsHead model) {
        ArgumentList result = ArgumentListBuilder.newBuilder()
            .opt("-n", Long.toString(model.getLines().orElse(null)))
            .files(model.getFileNames())
            .build();
        return result;
    }

    public static ArgsModular<ArgsHead> parse(String[] args) {
        ArgsHead model = ArgsParserPicocli.of(ArgsHead::new).parse(args);
        return new ArgsModular<>(model, ArgsHead::renderArgList);
    }
}
