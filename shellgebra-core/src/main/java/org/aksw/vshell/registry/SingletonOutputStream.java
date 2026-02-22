package org.aksw.vshell.registry;

import java.io.Closeable;
import java.io.OutputStream;

public interface SingletonOutputStream
    extends Closeable
{
    OutputStream outputStream();
}
