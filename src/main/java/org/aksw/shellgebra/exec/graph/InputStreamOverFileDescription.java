package org.aksw.shellgebra.exec.graph;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.input.ProxyInputStream;

public class InputStreamOverFileDescription
    extends ProxyInputStream
{
    protected FileDescription<InputStream> fd;

    public InputStreamOverFileDescription(FileDescription<InputStream> fd) {
        super(fd.getRaw());
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            fd.close();
        }
    }
}
