package org.aksw.vshell.shim.rdfconvert;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aksw.shellgebra.algebra.stream.op.Resolution1;

public class VProgRdfConvert {
    public static void main(String[] args) {
         RapperArgs model = RapperArgs.parse(new String[]{"-i", "rdfxml", "-o", "nt", "-", "http://foo.bar/"});
         ArgumentVector rapperCall = new ArgumentVector("/virt/rapper", model);

//         System.out.println(model);
//         System.out.println(model.toArgLine());

         System.out.println(rapperCall);

         // Find the first provider that accepts the arguments.
         List<String> cmdLine = new ArrayList<>();

         String RAPPER = "/virt/rapper";
         String BZIP2 = "/virt/bzip2";

         // Virtual tool -> domain view parser
         Map<String, ArgsParser<?>> argsParsers = new ConcurrentHashMap<>();
         argsParsers.put(RAPPER, ArgsParserPicocli.of(RapperArgs.class)); /** Factor out CmdArg renderer? */
         argsParsers.put(BZIP2, ArgsParserPicocli.of(GenericCodecArgs.class));

         Resolution1 x;
         
         // Resolution: Virtual commands can be resolved to
         // - java implementation of the command
         // - host paths
         // - docker image + path
         // - program names (essentially alt names) which then need resolution to paths

         // Probably need a ResolutionContext to track the process of resolution.

    }
}
