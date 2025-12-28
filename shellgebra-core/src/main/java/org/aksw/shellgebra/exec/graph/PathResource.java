package org.aksw.shellgebra.exec.graph;

import java.io.IOException;
import java.nio.file.Path;

import org.aksw.shellgebra.exec.PathLifeCycle;

public class PathResource
    // implements AutoCloseable
    implements PassiveResource
{
    private Path path;
    private PathLifeCycle lifeCycle;
    private volatile boolean isOpen = false;
    private Object lock = new Object();

    public PathResource(Path path, PathLifeCycle lifeCycle) {
        super();
        this.path = path;
        this.lifeCycle = lifeCycle;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public Path getPath() {
        return path;
    }

    /** Run the beforeExec method of the life cycle if not already open. */
    @Override
    public void open() throws IOException {
        synchronized (lock) {
            if (isOpen) {
                throw new IllegalStateException("Resource is already open");
            }
            lifeCycle.beforeExec(path);
            isOpen = true;
        }
    }
    @Override
    public void close() throws Exception {
        synchronized (lock) {
            isOpen = false;
            lifeCycle.afterExec(path);
        }
    }

    @Override
    public String toString() {
        return path + "(" + (isOpen() ? "open" : "closed") + ")";
    }
}
