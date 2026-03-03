package org.aksw.vshell.registry;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

public class OutputWrapper<X extends Output>
    implements Output
{
    private X delegate;

    public OutputWrapper(X delegate) {
        super();
        this.delegate = delegate;
    }

    protected X getDelegate() {
        return delegate;
    }

    @Override
    public void close() throws IOException {
        getDelegate().close();
    }

    @Override
    public Charset getWriterCharset() {
        return getDelegate().getWriterCharset();
    }

    @Override
    public boolean hasWriter() {
        return getDelegate().hasWriter();
    }

    @Override
    public boolean hasPrinter() {
        return getDelegate().hasPrinter();
    }

    @Override
    public OutputStream outputStream() {
        return getDelegate().outputStream();
    }

    @Override
    public BufferedWriter writer() {
        return getDelegate().writer();
    }

    @Override
    public BufferedWriter writer(Charset charset) {
        return getDelegate().writer(charset);
    }

    @Override
    public PrintStream printStream() {
        return getDelegate().printStream();
    }

    @Override
    public PrintStream printStream(Charset charset) {
        return getDelegate().printStream(charset);
    }

    @Override
    public void flush() throws IOException {
        getDelegate().flush();
    }
}
