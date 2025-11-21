package org.aksw.shellgebra.exec.graph;

import java.util.Arrays;
import java.util.Objects;

public class FdTable<T>
    implements AutoCloseable
{
    private FileDescription<T>[] fileDescriptions;

    public FdTable() {
        this(3);
    }

    @SuppressWarnings("unchecked")
    public FdTable(int initialSize) {
        this(new FileDescription[initialSize]);
    }

    public FdTable(FileDescription<T>[] fileDescriptions) {
        super();
        this.fileDescriptions = fileDescriptions;
    }

    public T getResource(int id) {
        return getFd(id).get();
    }


    public FileDescription<T> getFd(int id) {
        FileDescription<T> result = fileDescriptions[id];
        if (result == null) {
            throw new IllegalArgumentException("no such fd: " + id);
        }
        return result;
    }

    /** Duplicate all file descriptors in this table. */
    public synchronized FdTable<T> dup() {
        FileDescription<T>[] copy = dup(fileDescriptions);
        return new FdTable<>(copy);
    }

    public void closeFd(int id) {
        fileDescriptions[id].close();
        fileDescriptions[id] = null;
    }

    public void alloc(int n) {
        if (n > fileDescriptions.length) {
            fileDescriptions = Arrays.copyOf(fileDescriptions, n);
        }
    }

    public void setFd(int tgt, FileDescription<T> newer) {
        Objects.requireNonNull(newer);
        setFd(tgt, -1, newer);
    }

    public void dup2(int tgt, int src) {
        FileDescription<T> srcFd = fileDescriptions[src];
        if (srcFd == null) {
            throw new IllegalArgumentException("no fd with id " + src);
        }

        FileDescription<T> newer = srcFd.dup();
        setFd(tgt, src, newer);
    }


    private void setFd(int tgt, int dupSrc, FileDescription<T> newer) {
        alloc(tgt);

        FileDescription<T> old = fileDescriptions[tgt];
        if (old != null && old != newer) {
            old.close();
        }

        if (newer.isOpen()) {
            fileDescriptions[tgt] = newer;
        } else {
            throw new IllegalArgumentException("fd already closed - duplicated from " + dupSrc);
        }
    }

    private static <T> FileDescription<T>[] dup(FileDescription<T>[] fileDescriptions) {
        int n = fileDescriptions.length;
        @SuppressWarnings("unchecked")
        FileDescription<T>[] fds = new FileDescription[n];
        for (int i = 0; i < n; ++i) {
            FileDescription<T> orig = fileDescriptions[i];
            if (orig != null) {
                FileDescription<T> copy = orig.dup();
                if (copy.isOpen()) {
                    fds[i] = copy;
                }
            }
        }
        return fds;
    }

    @Override
    public void close() {
        for (FileDescription<?> fd : fileDescriptions) {
            if (fd != null) {
                fd.close();
            }
        }
    }
}
