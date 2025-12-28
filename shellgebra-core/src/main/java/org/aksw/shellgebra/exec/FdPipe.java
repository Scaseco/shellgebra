package org.aksw.shellgebra.exec;

import java.io.File;
import java.io.FileDescriptor;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * File descriptor based pipe.
 */
public interface FdPipe {
    InputStream getInputStream();
    OutputStream getOutputStream();

    int getReadFd();
    int getWriteFd();

    Path getReadEndProcPath();
    Path getWriteEndProcPath();

    File getReadEndProcFile();
    File getWriteEndProcFile();

    FileDescriptor getReadFileDescriptor();
    FileDescriptor getWriteFileDescriptor();
}
