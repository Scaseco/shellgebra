package org.aksw.shellgebra.shim.cmd;

import java.util.ArrayList;
import java.util.List;

import org.aksw.shellgebra.shim.core.ArgsModular;
import org.aksw.shellgebra.shim.core.ArgumentList;
import org.aksw.shellgebra.shim.core.ArgumentListBuilder;
import org.aksw.shellgebra.shim.picocli.ArgsParserPicocli;

import picocli.CommandLine.Parameters;

public class ArgsEcho {
    @Parameters(arity = "0..*", description = "Arguments")
    public List<String> args = new ArrayList<>();

    public List<String> getArgs() {
        return args;
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

    public static Boolean stdinTest(ArgsEcho model) {
        return false;
    }

    public static ArgsModular<ArgsEcho> parse(String[] args) {
        ArgsEcho model = ArgsParserPicocli.of(ArgsEcho::new).parse(args);
        return new ArgsModular<>(model, ArgsEcho::renderArgList, ArgsEcho::stdinTest);
    }
}
