package org.aksw.vshell.registry;

import org.aksw.vshell.shim.rdfconvert.Args;
import org.aksw.vshell.shim.rdfconvert.ArgsModular;
import org.aksw.vshell.shim.rdfconvert.ArgsParserPicocli;
import org.aksw.vshell.shim.rdfconvert.ArgumentList;
import org.aksw.vshell.shim.rdfconvert.ArgumentListBuilder;

import picocli.CommandLine.Option;

// This is a stub. It only supports -e to test existence of commands.
public class ArgsTest
    implements Args
{
    @Option(names = "-e", arity = "1", description = "Exit code 0 if command exists.")
    String e;

    public String getE() {
        return e;
    }

//    @Parameters(arity = "0..*", description = "File names")
//    public List<String> fileNames = new ArrayList<>();

//    public List<String> getFileNames() {
//        return fileNames;
//    }

    @Override
    public ArgumentList toArgList() {
        return renderArgList(this);
    }

    @Override
    public String toString() {
        return "ArgsTest [e=" + e + "]";
    }

    public static ArgumentList renderArgList(ArgsTest model) {
        ArgumentList result = ArgumentListBuilder.newBuilder()
            .opt("-e", model.getE())
            .build();
        return result;
    }

    public static ArgsModular<ArgsTest> parse(String[] args) {
        ArgsTest model = ArgsParserPicocli.of(ArgsTest::new).parse(args);
        return new ArgsModular<>(model, ArgsTest::renderArgList);
    }
}
