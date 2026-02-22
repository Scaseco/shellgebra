package org.aksw.shellgebra.io.pipe;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

import org.aksw.vshell.registry.Input;
import org.aksw.vshell.registry.Output;

public interface Pipe
    extends Closeable
{
    Input input();
    Output output();

    InputStream inputStream();
    OutputStream outputStream();

    /*
     * Convenience methods below, inspired by ProcessBuilder from Java 17+.
     */

    PrintStream printer();
    PrintStream printer(Charset charset);
    BufferedWriter writer();
    BufferedWriter writer(Charset charset);
    BufferedReader reader();
    BufferedReader reader(Charset charset);
}
