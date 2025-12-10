package org.aksw.vshell.registry;

import java.util.ArrayList;
import java.util.List;

import org.aksw.vshell.shim.rdfconvert.Args;
import org.aksw.vshell.shim.rdfconvert.ArgsModular;
import org.aksw.vshell.shim.rdfconvert.ArgsParserPicocli;
import org.aksw.vshell.shim.rdfconvert.ArgumentList;
import org.aksw.vshell.shim.rdfconvert.ArgumentListBuilder;

import picocli.CommandLine.Parameters;

public class ArgsEcho
    implements Args
{
    @Parameters(arity = "0..*", description = "Arguments")
    public List<String> args = new ArrayList<>();

    public List<String> getArgs() {
        return args;
    }

    @Override
    public ArgumentList toArgList() {
        return renderArgList(this);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [args=" + args + "]";
    }

    public static ArgumentList renderArgList(ArgsEcho model) {
        ArgumentList result = ArgumentListBuilder.newBuilder()
            .args(model.getArgs())
            .build();
        return result;
    }

    public static ArgsModular<ArgsEcho> parse(String[] args) {
        ArgsEcho model = ArgsParserPicocli.of(ArgsEcho::new).parse(args);
        return new ArgsModular<>(model, ArgsEcho::renderArgList);
    }
}
