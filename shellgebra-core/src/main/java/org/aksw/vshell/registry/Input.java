package org.aksw.vshell.registry;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Interface for optional character views over a live source.
 * The Input instance itself must be closed.
 *
 * Views must be used consistently: Once a reader with a specific charset has been obtained,
 * then it is not possible to obtain another reader view with a different charset.
 *
 * Closing the readers or the inputStream directly should be avoided and may raise an
 * {@link UnsupportedOperationException}.
 */
public interface Input
    extends Closeable
{
    InputStream inputStream();
    boolean hasReader();
    BufferedReader reader();
    BufferedReader reader(Charset charset);
    Charset getReaderCharset();

    /** Transfer remaining data to output. Reuses a reader if present. */
    void transferTo(Output output) throws IOException;
}
