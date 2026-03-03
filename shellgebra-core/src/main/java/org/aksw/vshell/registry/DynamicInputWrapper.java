package org.aksw.vshell.registry;

import java.io.IOException;
import java.nio.file.Path;

public class DynamicInputWrapper<X extends DynamicInput>
    extends InputWrapper<X>
    implements DynamicInput
{
    protected DynamicInputWrapper(X delegate) {
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
