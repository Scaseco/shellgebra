package org.aksw.vshell.registry;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

// TODO Wire up at least CompletableFuture<ProcessHandle> onExit()
public class ProcessHandleBase
    implements ProcessHandle
{
    @Override
    public Stream<ProcessHandle> children() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int compareTo(ProcessHandle other) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Stream<ProcessHandle> descendants() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean destroy() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean destroyForcibly() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Info info() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isAlive() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public CompletableFuture<ProcessHandle> onExit() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<ProcessHandle> parent() {
        // TODO Auto-generated method stub
        return Optional.empty();
    }

    @Override
    public long pid() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean supportsNormalTermination() {
        // TODO Auto-generated method stub
        return false;
    }

}
