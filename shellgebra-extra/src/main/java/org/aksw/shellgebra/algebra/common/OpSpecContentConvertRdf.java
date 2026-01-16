package org.aksw.shellgebra.algebra.common;

/**
 * RDF content conversion may require a base IRI.
 */
public record OpSpecContentConvertRdf(String sourceFormat, String targetFormat, String baseIri)
    implements OpSpec
{
}
