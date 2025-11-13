package org.aksw.vshell.registry;

import java.util.ArrayList;
import java.util.List;

import org.aksw.vshell.shim.rdfconvert.Args;
import org.aksw.vshell.shim.rdfconvert.ArgsModular;
import org.aksw.vshell.shim.rdfconvert.ArgsParserPicocli;
import org.aksw.vshell.shim.rdfconvert.ArgumentList;
import org.aksw.vshell.shim.rdfconvert.ArgumentListBuilder;

import picocli.CommandLine.Parameters;

public class ArgsCat
    implements Args
{
    @Parameters(arity = "0..*", description = "File names")
    public List<String> fileNames = new ArrayList<>();

    public List<String> getFileNames() {
        return fileNames;
    }

    @Override
    public ArgumentList toArgList() {
        return renderArgList(this);
    }

    @Override
    public String toString() {
        return "ArgsCat [fileNames=" + fileNames + "]";
    }

    public static ArgumentList renderArgList(ArgsCat model) {
        ArgumentList result = ArgumentListBuilder.newBuilder()
            .files(model.getFileNames())
            .build();
        return result;
    }

    public static ArgsModular<ArgsCat> parse(String[] args) {
        ArgsCat model = ArgsParserPicocli.of(ArgsCat::new).parse(args);
        return new ArgsModular<>(model, ArgsCat::renderArgList);
    }
}
