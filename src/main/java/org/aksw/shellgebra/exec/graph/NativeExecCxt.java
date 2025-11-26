package org.aksw.shellgebra.exec.graph;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.lang.ProcessBuilder.Redirect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import org.aksw.shellgebra.exec.PathLifeCycle;
import org.aksw.shellgebra.exec.PathLifeCycles;
import org.aksw.shellgebra.exec.graph.FdResource.FdResourceInputStream;
import org.aksw.shellgebra.exec.graph.FdResource.FdResourceOutputStream;

public class NativeExecCxt
    implements AutoCloseable
{
    private PathResource fd0;
    private PathResource fd1;
    private PathResource fd2;

    private boolean fd0OverridesInherit = false;
    private boolean fd1OverridesInherit = false;
    private boolean fd2OverridesInherit = false;

    private Thread dummyThread;
    private Runnable closeAction;


    private FdTable fdTable;

    private Thread inThread;
    private Thread outThread;
    private Thread errThread;

//    private OutputStream in;
//    private InputStream out;
//    private InputStream err;

    public NativeExecCxt(PathResource fd0, PathResource fd1, PathResource fd2,
            FdTable fdTable,
            boolean fd0OverridesInherit, boolean fd1OverridesInherit, boolean fd2OverridesInherit,
            Runnable closeAction) {
        super();
        this.fd0OverridesInherit = fd0OverridesInherit;
        this.fd1OverridesInherit = fd1OverridesInherit;
        this.fd2OverridesInherit = fd2OverridesInherit;

        this.fdTable = fdTable;

        this.fd0 = fd0;
        this.fd1 = fd1;
        this.fd2 = fd2;

        this.closeAction = closeAction;
    }

    public FdTable getFdTable() {
        return fdTable;
    }

    private void configureInput(Redirect redirect, PathResource fd, boolean fdOverridesInherit, Consumer<Redirect> redirectConsumer) {
        Type type = redirect.type();
        switch (type) {
        case PIPE:
            redirectConsumer.accept(Redirect.from(fd.getPath().toFile()));
            break;
        case INHERIT:
            if (fdOverridesInherit) {
                redirectConsumer.accept(Redirect.from(fd.getPath().toFile()));
            }
            break;
        default:
            // nothing to do.
        }
    }

    private void configureOutput(Redirect redirect, PathResource fd, boolean fdOverridesInherit, Consumer<Redirect> redirectConsumer) {
        Type type = redirect.type();
        switch (type) {
        case PIPE:
            redirectConsumer.accept(Redirect.to(fd.getPath().toFile()));
            break;
        case INHERIT:
            if (fdOverridesInherit) {
                redirectConsumer.accept(Redirect.to(fd.getPath().toFile()));
            }
            break;
        default:
            // nothing to do.
        }
    }

    public Thread setOutputReader(Consumer<InputStream> reader) {
        Thread thread = new Thread(() -> {
            try (InputStream in = getInputStream()) {
                reader.accept(in);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
        outThread = thread;
        return thread;
    }

    public Thread setErrorReader(Consumer<InputStream> reader) {
        Thread thread = new Thread(() -> {
            try (InputStream in = getErrorStream()) {
                reader.accept(in);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
        errThread = thread;
        return thread;
    }

    public Thread setInputSupplier(Consumer<OutputStream> inputSupplier) {
        Thread thread = new Thread(() -> {
            try (OutputStream out = getOutputStream()) {
                inputSupplier.accept(out);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
        inThread = thread;
        return thread;
    }

    public OutputStream getOutputStream() {
        new RuntimeException("here i am").printStackTrace();
        // fdTable.getFd(0).isOpen();
        // System.out.println("Write End - open: " + fdTable.getFd(0).isOpen());
        return ((FdResourceOutputStream)fdTable.getResource(0)).outputStream();
    }

    public InputStream getInputStream() {
        return ((FdResourceInputStream)fdTable.getFd(1).get()).inputStream();
    }

    public InputStream getErrorStream() {
        return ((FdResourceInputStream)fdTable.getFd(2).get()).inputStream();
    }

    public ProcessBuilder configure(ProcessBuilder processBuilder) {
        configureInput(processBuilder.redirectInput(), fd0, fd0OverridesInherit, processBuilder::redirectInput);
        configureOutput(processBuilder.redirectOutput(), fd1, fd1OverridesInherit, processBuilder::redirectOutput);
        configureOutput(processBuilder.redirectError(), fd2, fd2OverridesInherit, processBuilder::redirectError);
        return processBuilder;
    }

    static Timer timer = null; //new Timer();

    public static NativeExecCxt create(Path basePath) throws IOException {
        return create(basePath, true, true, true);
    }

    public static NativeExecCxt create(Path basePath, boolean fd0OverridesInherit, boolean fd1OverridesInherit, boolean fd2OverridesInherit) throws IOException {
        Path fd0 = basePath.resolve("fd0");
        Path fd1 = basePath.resolve("fd1");
        Path fd2 = basePath.resolve("fd2");

        PathLifeCycle lifeCycle = PathLifeCycles.deleteAfterExec(PathLifeCycles.namedPipe());
        PathResource rfd0 = new PathResource(fd0, lifeCycle);
        PathResource rfd1 = new PathResource(fd1, lifeCycle);
        PathResource rfd2 = new PathResource(fd2, lifeCycle);

        rfd0.open();
        rfd1.open();
        rfd2.open();

        Runnable[] closer = {null};

        // Use a thread to open the process-facing ends of the pipes and hold them.
        Thread internalPipeEndOpenerThread = new Thread(() -> {
            try {
                // FileDescriptor.in
                // Open READ end of input pipe
                InputStream xfd0 = new FileInputStream(rfd0.getPath().toFile()); // Files.newInputStream(rfd0.getPath());

                if (false) {
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            System.out.println("Hijacking input");
                            PGroup.readerThread(xfd0, "hijack: ").start();
                        }
                    }, 5000);
                }

                OutputStream xfd1 = Files.newOutputStream(rfd1.getPath());
                OutputStream xfd2 = Files.newOutputStream(rfd2.getPath());

                closer[0] = () -> {
                    System.out.println("Closing ends of process-facing pipes.");
                    try {
                        xfd0.close();
                        xfd1.close();
                        xfd2.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                };
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Pipe ends openend - pipe end opener thread terminated.");
        });
        internalPipeEndOpenerThread.start();

        FdTable fdTable = new FdTable<>();
        // Open the client facing ends of the pipes.
        fdTable.setFd(0, FileDescriptions.of(Files.newOutputStream(rfd0.getPath())));

        // Cannot connect to a file input stream if it hasn't been opened for writing
        fdTable.setFd(1, FileDescriptions.of(Files.newInputStream(rfd1.getPath())));
        fdTable.setFd(2, FileDescriptions.of(Files.newInputStream(rfd2.getPath())));

        try {
            internalPipeEndOpenerThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Got internal ends");

        Runnable closeAction = closer[0];

        return new NativeExecCxt(rfd0, rfd1, rfd2, fdTable, fd0OverridesInherit, fd1OverridesInherit, fd2OverridesInherit, closeAction);
    }

    @Override
    public void close() throws Exception {
        fdTable.close();
        fd0.close();
        fd1.close();
        fd2.close();
        Runnable ca = closeAction;
        if (ca != null) {
            ca.run();
        }

        if (errThread != null) { errThread.join(); }
        if (outThread != null) { outThread.join(); }
    }
}
