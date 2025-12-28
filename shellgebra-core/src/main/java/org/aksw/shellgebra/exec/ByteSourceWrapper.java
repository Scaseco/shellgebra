package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.io.InputStream;

import com.google.common.io.ByteSource;

public class ByteSourceWrapper
    extends ByteSource
{
    private ByteSource delegate;

    public ByteSourceWrapper(ByteSource delegate) {
        super();
        this.delegate = delegate;
    }

    public ByteSource getDelegate() {
        return delegate;
    }

    @Override
    public InputStream openStream() throws IOException {
        ByteSource d = getDelegate();
        return d.openStream();
    }
}
