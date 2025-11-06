package org.aksw.shellgebra.exec.io;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FdTable implements AutoCloseable {
    private final Map<Integer, FdEntry> entries = new ConcurrentHashMap<>();

    public void put(FdEntry entry) throws Exception {
        int fd = entry.getFd();
        entries.put(fd, entry);

    }

    public FdEntry get(int fd) {
        return entries.get(fd);
    }

    public void closeFd(int fd) throws Exception {

    }

    @Override
    public void close() throws Exception {

    }
}
