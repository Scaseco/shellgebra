package org.aksw.shellgebra.registry.content;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.aksw.jenax.arq.util.lang.RDFLanguagesEx;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;

public class CommandLineArgumentsBuilderRapper
    implements CommandLineArgumentsBuilder
{
    protected Map<Lang, String> langMap = initLangMap(new LinkedHashMap<>());

    public static Map<Lang, String> initLangMap(Map<Lang, String> map) {
        map.put(Lang.RDFXML, "rdfxml");
        map.put(Lang.NTRIPLES, "ntriples");
        map.put(Lang.TURTLE, "turtle");
        map.put(Lang.TRIG, "trig");
        map.put(Lang.NQUADS, "nquads");
        return map;
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

        String tmpSrcArg = langMap.get(srcLang);
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
        String tmpTgtArg = langMap.get(tgtLang);
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
        return List.of("-i", srcFmtArg, "-o", tgtFmtArg, "baseUri");
    }
}
