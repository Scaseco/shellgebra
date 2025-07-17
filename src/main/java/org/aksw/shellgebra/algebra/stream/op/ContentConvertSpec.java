package org.aksw.shellgebra.algebra.stream.op;

// XXX Do we need a base IRI to parameterize RDF conversions?
public record ContentConvertSpec(String sourceFormat, String targetFormat, String baseIri) {
}
