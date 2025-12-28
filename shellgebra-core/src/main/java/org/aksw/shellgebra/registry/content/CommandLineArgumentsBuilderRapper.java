package org.aksw.shellgebra.registry.content;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.aksw.jenax.arq.util.lang.RDFLanguagesEx;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;

// TODO Move this to jenax!

/** Convert parameters of OpStreamContentConvert to arguments for a command invocation.*/
public class CommandLineArgumentsBuilderRapper
    implements CommandLineArgumentsBuilder
{
    protected Map<Lang, String> inputLangMap = initInputLangMap(new LinkedHashMap<>());

    public static Map<Lang, String> initInputLangMap(Map<Lang, String> inMap) {
        inMap.put(Lang.RDFXML, "rdfxml");
        inMap.put(Lang.NTRIPLES, "ntriples");
        inMap.put(Lang.TURTLE, "turtle");
        inMap.put(Lang.TRIG, "trig");
        inMap.put(Lang.NQUADS, "nquads");
        return inMap;
    }

        /*
        ntriples        N-Triples (default)
        turtle          Turtle Terse RDF Triple Language
        nquads          N-Quads
        rdfxml          RDF/XML
        ---
        rdfxml-xmp      RDF/XML (XMP Profile)
        rdfxml-abbrev   RDF/XML (Abbreviated)
        mkr             mKR my Knowledge Representation Language
        rss-1.0         RSS 1.0
        atom            Atom 1.0
        dot             GraphViz DOT format
        json-triples    RDF/JSON Triples
        json            RDF/JSON Resource-Centric
        html            HTML Table
        */
    public static Map<Lang, String> initOutputLangMap(Map<Lang, String> outMap) {
        outMap.put(Lang.NTRIPLES, "ntriples");
        outMap.put(Lang.TURTLE, "turtle");
        outMap.put(Lang.NQUADS, "nquads");
        outMap.put(Lang.RDFXML, "rdfxml");
        return outMap;
    }

    // protected String srcLang;
    // protected String tgtFormat;
    protected String baseUri;

    protected String srcFmtArg;
    protected String tgtFmtArg;

    public CommandLineArgumentsBuilderRapper() {
        super();
    }

    public CommandLineArgumentsBuilderRapper setSrcLang(String srcLangStr) {
        Lang srcLang = RDFLanguagesEx.findLang(srcLangStr);
        if (srcLang == null) {
            throw new IllegalArgumentException("Could not resolve argument to a jena lang: " + srcLangStr);
        }

        String tmpSrcArg = inputLangMap.get(srcLang);
        if (tmpSrcArg == null) {
            throw new IllegalArgumentException("Could not resolve argument to a rapper input format value: " + srcLangStr);
        }

        this.srcFmtArg = tmpSrcArg;
        return this;
    }

    public CommandLineArgumentsBuilderRapper setTgtFormat(String tgtFormatStr) {
        RDFFormat tgtFormat = RDFLanguagesEx.findRdfFormat(tgtFormatStr);
        if (tgtFormat == null) {
            throw new IllegalArgumentException("Could not resolve argument to a jena rdf format: " + tgtFormatStr);
        }

        Lang tgtLang = tgtFormat.getLang();
        String tmpTgtArg = inputLangMap.get(tgtLang);
        if (tmpTgtArg == null) {
            throw new IllegalArgumentException("Could not resolve argument to a rapper input format value: " + tgtFormatStr);
        }
        this.tgtFmtArg = tmpTgtArg;
        return this;
    }

    public CommandLineArgumentsBuilderRapper setBaseUri(String baseUri) {
        this.baseUri = baseUri;
        return this;
    }

    @Override
    public List<String> build() {
        return List.of("-i", srcFmtArg, "-o", tgtFmtArg, "-", "baseUri");
    }
}
