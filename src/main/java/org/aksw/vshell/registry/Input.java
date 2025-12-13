package org.aksw.vshell.registry;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public interface Input
    extends Closeable
{
    InputStream inputStream();
    boolean hasReader();
    BufferedReader reader();
    BufferedReader reader(Charset charset);
    Charset getReaderCharset();

    void transferTo(Output output) throws IOException;
}
