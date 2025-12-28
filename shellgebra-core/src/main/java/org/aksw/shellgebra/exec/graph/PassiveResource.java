package org.aksw.shellgebra.exec.graph;

import java.io.IOException;

public interface PassiveResource
    extends AutoCloseable
{
    void open() throws IOException;
}
