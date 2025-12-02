package org.aksw.shellgebra.exec.graph;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.output.ProxyOutputStream;

public class OutputStreamOverFileDescription
    extends ProxyOutputStream
{
    protected FileDescription<OutputStream> fd;

    public OutputStreamOverFileDescription(FileDescription<OutputStream> fd) {
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
