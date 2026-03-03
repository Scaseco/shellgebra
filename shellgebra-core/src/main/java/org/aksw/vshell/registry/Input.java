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
 * FIXME Ideally it should be close shielder wrappers - we yet need to proxy BufferedReader.
 * Each call to inputStream or reader returns a fresh close-shielded wrapper over
 * the same underlying resource. It is good practice to close the inputStream or reader.
 *
 *
 * You must close the Input instance itself to close the underlying resource.
 */
public interface Input
    extends Closeable
{
    InputStream inputStream();
    boolean hasReader();
    BufferedReader reader();
    BufferedReader reader(Charset charset);
    Charset getReaderCharset();

    /**
     * Transfer remaining data to output.
     * Reuses a previously acquired reader if present.
     * Otherwise, falls back to using the input stream.
     */
    void transferTo(Output output) throws IOException;
}
