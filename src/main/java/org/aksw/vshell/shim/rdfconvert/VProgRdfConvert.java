package org.aksw.vshell.shim.rdfconvert;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aksw.shellgebra.algebra.stream.op.Resolution1;

public class VProgRdfConvert {
    public static void main(String[] args) {
        Args bzip2DecodeModel = GenericCodecArgs.parse(new String[]{"-d"});
        ArgumentVector bzip2DecodeCall = new ArgumentVector("/virt/bzip2", bzip2DecodeModel);

        Args rapperModel = RapperArgs.parse(new String[]{"-i", "rdfxml", "-o", "nt", "/home/user/file.rdf", "http://foo.bar/"});
        ArgumentVector rapperCall = new ArgumentVector("/virt/rapper", rapperModel);

        System.out.println(bzip2DecodeCall);
        System.out.println(rapperCall);

        // Find the first provider that accepts the arguments.
        String RAPPER = "/virt/rapper";
        String BZIP2 = "/virt/bzip2";

         // Note that argument parsers only parse concrete arguments -
         // Shell expressions such as "${FOO} need to be resolved first."

         // Virtual tool -> domain view parser
         Map<String, ArgsParser<?>> argsParsers = new ConcurrentHashMap<>();
         argsParsers.put(RAPPER, ArgsParserPicocli.of(RapperArgs::new)); /** Factor out CmdArg renderer? */
         argsParsers.put(BZIP2, ArgsParserPicocli.of(GenericCodecArgs::new));

         // CmdOpExec now requires tool name and ArgumentList...

         Resolution1 x;

         // Resolution: Virtual commands can be resolved to
         // - java implementation of the command
         // - host paths
         // - docker image + path
         // - program names (essentially alt names) which then need resolution to paths

         // Probably need a ResolutionContext to track the process of resolution.

    }
}
