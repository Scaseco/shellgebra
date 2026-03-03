package org.aksw.vshell.registry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class InputWrapper<X extends Input>
    implements Input
{
    private X delegate;

    protected InputWrapper(X delegate) {
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
    public InputStream inputStream() {
        return getDelegate().inputStream();
    }

    @Override
    public boolean hasReader() {
        return getDelegate().hasReader();
    }

    @Override
    public BufferedReader reader() {
        return getDelegate().reader();
    }

    @Override
    public BufferedReader reader(Charset charset) {
        return getDelegate().reader(charset);
    }

    @Override
    public Charset getReaderCharset() {
        return getDelegate().getReaderCharset();
    }

    @Override
    public void transferTo(Output output) throws IOException {
        getDelegate().transferTo(output);
    }
}
