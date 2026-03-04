package org.aksw.vshell.registry;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ProcessOverFuture
    extends ProcessWrapper<Process>
{
    private CompletableFuture<List<Process>> processFuture;
    private CompletableFuture<Process> onExitFuture = new CompletableFuture<>();

    public static Process ofOne(CompletableFuture<Process> process) {
        CompletableFuture<List<Process>> tmp = process.thenApply(List::of);
        return ofList(tmp);
    }

    public static Process ofList(CompletableFuture<List<Process>> processFuture) {
        return new ProcessOverFuture(processFuture);
    }

    protected ProcessOverFuture(CompletableFuture<List<Process>> processFuture) {
        super(null);
        this.processFuture = processFuture;
        this.processFuture.whenComplete((ps, t) -> {
            if (t == null) {
                Process lastP = ps.getLast();
                // p.onExit().then;
                lastP.onExit().whenComplete((pp, tt) -> {
                    if (tt == null) {
                        onExitFuture.complete(pp);
                    } else {
                        onExitFuture.completeExceptionally(tt);
                    }
                });
            } else {
                onExitFuture.completeExceptionally(t);
            }
        });
    }

    @Override
    public Process getDelegate() {
        try {
            List<Process> ps = processFuture.get();
            return ps.getLast();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompletableFuture<Process> onExit() {
        return onExitFuture;
    }
}
