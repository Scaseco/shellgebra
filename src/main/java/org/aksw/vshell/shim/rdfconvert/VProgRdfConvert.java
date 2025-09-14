package org.aksw.vshell.shim.rdfconvert;


import java.util.ArrayList;
import java.util.List;

public class VProgRdfConvert {
    public static void main(String[] args) {
         RapperArgs model = RapperArgs.parse(new String[]{"-i", "rdfxml", "-o", "nt", "-", "http://foo.bar/"});
         System.out.println(model);
         System.out.println(model.toArgLine());

         // Find the first provider that accepts the arguments.
         List<String> cmdLine = new ArrayList<>();
    }
}
