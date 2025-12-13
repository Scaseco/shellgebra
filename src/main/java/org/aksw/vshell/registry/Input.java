package org.aksw.vshell.registry;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;

public interface Input
    extends Closeable
{
    InputStream inputStream();
    boolean hasReader();
    Reader reader();
    Reader reader(Charset charset);
    Charset getReaderCharset();

    void transferTo(Output output) throws IOException;
}
