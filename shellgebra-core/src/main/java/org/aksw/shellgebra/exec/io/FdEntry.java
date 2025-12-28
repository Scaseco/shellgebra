package org.aksw.shellgebra.exec.io;

import java.io.InputStream;
import java.io.OutputStream;

final class FdEntry implements AutoCloseable {
    public enum FdType {
        IN, OUT, INOUT
    }

    public enum ClosePolicy {
        OWNED, BORROWED
    }

    private final int fd;
    private final FdType type;
    private final ClosePolicy closePolicy;
    private final InputStream in; // nullable
    private final OutputStream out; // nullable

    public FdEntry(int fd, FdType type, ClosePolicy closePolicy, InputStream in, OutputStream out) {
        this.fd = fd;
        this.type = type;
        this.closePolicy = closePolicy;
        this.in = in;
        this.out = out;
    }

    public int getFd() {
        return fd;
    }

    public FdType getType() {
        return type;
    }

    public ClosePolicy getClosePolicy() {
        return closePolicy;
    }

    public InputStream getIn() {
        return in;
    }

    public OutputStream getOut() {
        return out;
    }

    @Override
    public void close() throws Exception {
        if (closePolicy == ClosePolicy.OWNED) {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ignored) {
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception ignored) {
            }
        }
    }
}
