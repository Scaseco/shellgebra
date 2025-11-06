package org.aksw.shellgebra.exec.io;

import java.io.InputStream;
import java.io.OutputStream;

// Not sure whether we need this abstraction:
// We can't set an input source - instead we have to get the output stream and write to it.
// With the stage API we can set a source - but i think an abstraction like the FdTable would be useful.
public class ProcessInputStreamTransform
    extends Process
{

    @Override
    public OutputStream getOutputStream() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream getInputStream() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream getErrorStream() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int waitFor() throws InterruptedException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int exitValue() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }
}
