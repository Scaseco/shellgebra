package org.aksw.shellgebra.shim.cmd;

import org.aksw.commons.io.util.stream.InputStreamTransform;
import org.aksw.commons.io.util.stream.OutputStreamTransform;

public record JavaStreamTransform(
    InputStreamTransform inputStreamTransform,
    OutputStreamTransform outputStreamTransform)
{
}
