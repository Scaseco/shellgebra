package org.aksw.vshell.registry;

import java.util.ArrayList;
import java.util.List;

import org.aksw.vshell.shim.rdfconvert.Args;
import org.aksw.vshell.shim.rdfconvert.ArgsModular;
import org.aksw.vshell.shim.rdfconvert.ArgsParserPicocli;
import org.aksw.vshell.shim.rdfconvert.ArgumentList;
import org.aksw.vshell.shim.rdfconvert.ArgumentListBuilder;

import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

public class ArgsWhich
    implements Args
{
    @Option(names = "-a", description = "Print all matching pathnames of each argument")
    boolean all;

    @Option(names = "-s", description = "Silently return 0 if all of the executables were found or 1 otherwise")
    boolean silent;

    @Parameters(arity = "0..*", description = "File names")
    public List<String> fileNames = new ArrayList<>();

    public boolean isAll() {
        return all;
    }

    public boolean isSilent() {
        return silent;
    }

    public List<String> getFileNames() {
        return fileNames;
    }

    @Override
    public ArgumentList toArgList() {
        return renderArgList(this);
    }

    @Override
    public String toString() {
        return "ArgsWhich [all=" + all + ", silent=" + silent + ", files=" + fileNames + "]";
    }

    public static ArgumentList renderArgList(ArgsWhich model) {
        ArgumentList result = ArgumentListBuilder.newBuilder()
            .ifTrue(model.isAll(), "-a")
            .ifTrue(model.isSilent(), "-s")
            .args(model.getFileNames())
            .build();
        return result;
    }

    public static ArgsModular<ArgsWhich> parse(String[] args) {
        ArgsWhich model = ArgsParserPicocli.of(ArgsWhich::new).parse(args);
        return new ArgsModular<>(model, ArgsWhich::renderArgList);
    }
}
