package org.aksw.vshell.registry;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

public interface Output
    extends Closeable
{
    Charset getWriterCharset();

    boolean hasWriter();
    boolean hasPrinter();
    OutputStream outputStream();

    BufferedWriter writer();
    BufferedWriter writer(Charset charset);

    PrintStream printStream();
    PrintStream printStream(Charset charset);

    void flush() throws IOException;
}
