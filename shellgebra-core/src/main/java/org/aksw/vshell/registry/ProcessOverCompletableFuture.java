package org.aksw.vshell.registry;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class ProcessOverCompletableFuture
    extends ProcessBase
{
    private CompletableFuture<Integer> asyncComputation;

    public ProcessOverCompletableFuture(CompletableFuture<Integer> asyncComputation) {
        super();
        this.asyncComputation = asyncComputation;
    }

    public static Process of(Supplier<Integer> supplier) {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(supplier);
        return new ProcessOverCompletableFuture(future);
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
