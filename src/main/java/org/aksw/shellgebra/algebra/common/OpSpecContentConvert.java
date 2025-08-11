package org.aksw.shellgebra.algebra.common;

// XXX Do we need a base IRI to parameterize RDF conversions?
public record OpSpecContentConvert(String sourceFormat, String targetFormat, String baseIri)
    implements OpSpec
{
}
