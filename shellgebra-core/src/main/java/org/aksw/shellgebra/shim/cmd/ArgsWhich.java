package org.aksw.shellgebra.shim.cmd;

import java.util.ArrayList;
import java.util.List;

import org.aksw.shellgebra.shim.core.ArgsModular;
import org.aksw.shellgebra.shim.core.ArgumentList;
import org.aksw.shellgebra.shim.core.ArgumentListBuilder;
import org.aksw.shellgebra.shim.picocli.ArgsParserPicocli;

import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

public class ArgsWhich
//     implements Args
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

//    @Override
//    public ArgumentList toArgList() {
//        return renderArgList(this);
//    }

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

    public static Boolean stdinTest(ArgsWhich args) {
        return args.getFileNames().isEmpty() || args.getFileNames().contains("-");
    }

    public static ArgsModular<ArgsWhich> parse(String[] args) {
        ArgsWhich model = ArgsParserPicocli.of(ArgsWhich::new).parse(args);
        return new ArgsModular<>(model, ArgsWhich::renderArgList, ArgsWhich::stdinTest);
    }
}
