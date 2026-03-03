package org.aksw.vshell.registry;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class ProcessOverCompletableFuture
    extends ProcessBase
{
    private CompletableFuture<Integer> asyncComputation;
    private CompletableFuture<Process> onExit = new CompletableFuture<>();

    public ProcessOverCompletableFuture(CompletableFuture<Integer> asyncComputation, OutboundIo outboundIo) {
        super(outboundIo);
        this.asyncComputation = Objects.requireNonNull(asyncComputation);
        asyncComputation.handle((v, t) -> {
            Optional.ofNullable(t)
                .ifPresentOrElse(onExit::completeExceptionally, () -> onExit.complete(this));
            return null;
        });
    }

    public static Process of(OutboundIo outboundIo, Supplier<Integer> supplier) {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(supplier);
        return new ProcessOverCompletableFuture(future, outboundIo);
    }

    @Override
    public int waitFor() throws InterruptedException {
        try {
            int unusedExitValue = asyncComputation.get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return exitValue();
    }

    @Override
    public void destroy() {
        asyncComputation.cancel(true);
    }

    @Override
    public CompletableFuture<Process> onExit() {
        return onExit;
    }

    @Override
    public int exitValue() {
        if (asyncComputation.isDone()) {
            int exitValue;
            try {
                exitValue = asyncComputation.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            return exitValue;
        }

        throw new IllegalThreadStateException("Thread has not yet terminated");
    }
}
