package org.aksw.vshell.registry;

import org.aksw.vshell.shim.rdfconvert.ArgsModular;
import org.aksw.vshell.shim.rdfconvert.ArgsParserPicocli;
import org.aksw.vshell.shim.rdfconvert.ArgumentList;
import org.aksw.vshell.shim.rdfconvert.ArgumentListBuilder;

import picocli.CommandLine.Option;

/**
 * command -v bash
 * /usr/bin/bash
 */
public class ArgsCommand {
    @Option(names = "-v", arity = "1", description = "Exit code 0 if command exists.")
    private String e;

    public String getE() {
        return e;
    }

    @Override
    public String toString() {
        return "ArgsTest [e=" + e + "]";
    }

    public static ArgumentList renderArgList(ArgsCommand model) {
        ArgumentList result = ArgumentListBuilder.newBuilder()
            .opt("-e", model.getE())
            .build();
        return result;
    }

    public static ArgsModular<ArgsCommand> parse(String[] args) {
        ArgsCommand model = ArgsParserPicocli.of(ArgsCommand::new).parse(args);
        return new ArgsModular<>(model, ArgsCommand::renderArgList, a -> false);
    }
}
