package org.aksw.vshell.registry;

public class ProcessOverThread
    extends ProcessBase
{
    private Thread thread;

    public ProcessOverThread() {
        super();
    }

    public void setThread(Thread thread) {
        this.thread = thread;
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
}
