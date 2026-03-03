package org.aksw.vshell.registry;

import java.io.IOException;
import java.nio.file.Path;

public class DynamicOutputWrapper<X extends DynamicOutput>
    extends OutputWrapper<X>
    implements DynamicOutput
{
    public DynamicOutputWrapper(X delegate) {
        super(delegate);
    }

    @Override
    public boolean hasFile() {
        return getDelegate().hasFile();
    }

    @Override
    public Path getFile() throws IOException {
        return getDelegate().getFile();
    }
}
