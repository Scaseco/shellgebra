package org.aksw.vshell.registry;

import java.io.Closeable;
import java.io.InputStream;

public interface SingletonInputStream
    extends Closeable
{
    InputStream inputStream();
}
