package org.aksw.shellgebra.algebra.common;

/** OpSpec to turn a file into a byte stream. Effectively an abstract 'cat' operation. */
public record OpSpecFile(String name) implements OpSpec
{
}
