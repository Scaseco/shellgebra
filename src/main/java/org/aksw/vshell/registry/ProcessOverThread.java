package org.aksw.vshell.registry;

import java.io.IOException;

import org.aksw.shellgebra.exec.IProcessBuilderCore;
import org.aksw.shellgebra.exec.graph.ProcessRunner;

public class ProcessOverThread
    extends ProcessBase
{
    private Thread thread;
    private volatile Process actualProcess;

    public ProcessOverThread() {
        super();
    }

    void setThread(Thread thread) {
        this.thread = thread;
    }

    void setActualProcess(Process actualProcess) {
        this.actualProcess = actualProcess;
    }

    @Override
    public int waitFor() throws InterruptedException {
        thread.join();
        return exitValue();
    }

    @Override
    public void destroy() {
        thread.interrupt();
    }

    public static Process startInThread(IProcessBuilderCore<?> processBuilder, ProcessRunner context) {
        ProcessOverThread result = new ProcessOverThread();
        Runnable runnable = () -> {
            try {
                Process actualProcess = processBuilder.start(context);
                result.setActualProcess(actualProcess);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        Thread thread = new Thread(runnable);
        result.setThread(thread);
        thread.start();
        return result;
    }
}
