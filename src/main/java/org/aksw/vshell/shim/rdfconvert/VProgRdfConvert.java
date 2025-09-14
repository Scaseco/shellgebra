package org.aksw.vshell.shim.rdfconvert;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import picocli.CommandLine;

public class VProgRdfConvert {

    public static void main(String[] args) {

         RapperArgs model = new RapperArgs();
         CommandLine cmd = new CommandLine(model);
         cmd.parseArgs("-i", "rdfxml", "-o", "nt", "-", "http://foo.bar/");
         System.out.println(model);

         // Find the first provider that accepts the arguments.

         List<String> cmdLine = new ArrayList();
         Optional.ofNullable(model.inputFile).ifPresent(v -> cmdLine.addAll(List.of("-i", v)));
    }
}
