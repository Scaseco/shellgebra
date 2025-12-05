package org.aksw.shellgebra.exec.graph;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;
import java.lang.ProcessBuilder.Redirect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.function.Consumer;

import org.aksw.shellgebra.exec.PathLifeCycle;
import org.aksw.shellgebra.exec.PathLifeCycles;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectFileDescription;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectIn;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectJava;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectOut;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectPBF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

interface PBF {
    JRedirect redirectIn();
    JRedirect redirectOut();
    JRedirect redirectErr();

    PB createProcessBuilder();
}

// Combine a file name which can be passed directly to processes with the corresponding java streams.
class NamedPipeJava {
    private Path path;
    private InputStream readEnd;
    private OutputStream writeEnd;

    // public
}

// One visitor is to turn a redirect into a FileDescription for java-based execution.
// The other is to transform it into one for system-based execution.

// So for system-based execution: A java input stream must go to a named pipe so that system processes can pick it up
// This means,
class RedirectVisitorToDescription
    implements JRedirectVisitor<FileDescription<FdResource>> {

    // private PExecCxt execCxt;
    private FileDescription<FdResource> base;

    @Override
    public FileDescription<FdResource> visit(JRedirectJava redirect) {
        Redirect r = redirect.redirect();
        Type type = r.type();
        FileDescription<FdResource> result;
        try {
            result = switch (type) {
                case PIPE -> throw new UnsupportedOperationException("Redirect type 'PIPE' is only allowed on outer level");
                case INHERIT -> base.dup();
                case READ -> FileDescriptions.of(Files.newInputStream(r.file().toPath()));
                case WRITE -> FileDescriptions.of(Files.newOutputStream(r.file().toPath()));
                case APPEND -> FileDescriptions.of(Files.newOutputStream(r.file().toPath(), StandardOpenOption.APPEND));
                default -> throw new UnsupportedOperationException("Unknown type: " + type);
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public FileDescription<FdResource> visit(JRedirectFileDescription redirect) {
        return redirect.fileDescription();
    }

    @Override
    public FileDescription<FdResource> visit(JRedirectIn redirect) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileDescription<FdResource> visit(JRedirectOut redirect) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileDescription<FdResource> visit(JRedirectPBF redirect) {
        // TODO Auto-generated method stub
        return null;
    }
}


abstract class PBFBase
    implements PBF
{
    private JRedirect redirectIn;
    private JRedirect redirectOut;
    private JRedirect redirectErr;

    @Override
    public JRedirect redirectIn() {
        return redirectIn;
    }

    @Override
    public JRedirect redirectOut() {
        return redirectOut;
    }

    @Override
    public JRedirect redirectErr() {
        return redirectErr;
    }
}

class NativePBF
    // implements PBF
    extends PBFBase
{
    private String[] command;

    public NativePBF(String[] command) {
        super();
        this.command = command.clone();
    }

    @Override
    public PB createProcessBuilder() {
        return new NativePB(command);
    }

    public static NativePBF of(String...argv) {
        return new NativePBF(argv);
    }
}

/**
 * ProcessBuilder for the Java-native ProcessBuilder.
 *
 * Java-based:
 * stdin:
 * If the parent process supplies an InputStream, and the process uses INHERIT, then
 * change the process' input to pipe and setup a pumper thread.
 *
 * Details: If the parent only supplies in InputStream, then there are two ways to connect a native process:
 * <ul>
 *   <li>inputStream -&gt; pumper thread -&gt; FileOutputStream -&gt; namedPipe -&gt; pass to process as filename.</li>
 *   <li>inputStream -&gt; pumber thread -&gt; PIPE of process.
 * </ul>
 * Actually, variant 2 is the same as 1, except that the PIPE hides the rest.
 *
 * stdout:
 * If the process builder uses INHERIT and the parent uses a Java OutputStream (without a named pipe) then...
 * setup the process to use PIPE, and use a pumper thread to write to the parent output stream.
 *
 * If the parent uses a named pipe, then pass the name of the named pipe to the process builder.
 * Internally Java will open a FileOutputStream to the named pipe.
 *
 */
class NativePB
    implements PB
{
    private String[] command;
    private JRedirect redirectIn;
    private JRedirect redirectOut;
    private JRedirect redirectErr;

    public NativePB(String... command) {
        super();
        this.command = command.clone();
    }

    @Override
    public void redirectInput(JRedirect redirect) {
        this.redirectIn = redirect;
    }

    @Override
    public void redirectOutput(JRedirect redirect) {
        this.redirectOut = redirect;
    }

    @Override
    public void redirectError(JRedirect redirect) {
        this.redirectErr = redirect;
    }

    public JRedirect redirectIn() {
        return redirectIn;
    }

    public JRedirect redirectOut() {
        return redirectOut;
    }

    public JRedirect redirectErr() {
        return redirectErr;
    }

//    @Override
//    public PB createProcessBuilder() {
//        return new NativePB(command);
//    }

    @Override
    public Process start(ProcessRunner runner) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = runner.start(processBuilder);
        return process;
    }

//    @Override
//    public Process start() {
//        // TODO Auto-generated method stub
//        return null;
//    }

//    @Override
//    public Process start(PExecCxt execCxt) {
//        // TODO Auto-generated method stub
//        return null;
//    }
}

class PProcessBuilderFactoryBase
    extends PBFBase
{
    private PRedirect err[]; // Design from ProcessBuilder: init to {pipe, pipe, pipe}
//	private PRedirect err;
//	private PR
    public PBF redirectError(PRedirect redirect) {
        return this;
    }

    public PBF redirectOut(PRedirect redirect) {
        return this;
    }

    public PBF redirectIn(PRedirect redirect) {
        return this;
    }

    @Override
    public PB createProcessBuilder() {
        // TODO Auto-generated method stub
        return null;
    }
}

/**
 * The JRedirects are possibly high level specifications.
 * upon execution they will be turned into file descriptions (e.g. over named pipes)
 * for use with the process builder.
 */
interface PB {
    // TODO The redirects need to be low level redirects - so probably just the java redirects plus input/output streams.
    // The point is, that the low level redirect should still make it possible for the process to decide on
    // which form it wants - e.g. a java process might want a std to be a direct input stream, whereas
    // a native process want's the name of a named pipe!!!
    // FIXME Solved this!
    void redirectInput(JRedirect redirect);
    void redirectOutput(JRedirect redirect);
    void redirectError(JRedirect redirect);

    // PB createProcessBuilder();

    Process start(ProcessRunner context) throws IOException;
}


class ClientCxt {
    // PExecCxt
}

interface PExecCxt {
    JRedirect in();
    JRedirect out();
    JRedirect err();
}

class PExecutor
    // extends Process
{
    PExecCxt context; // These are life resources

    // Names of the named pipes that can be passed to system processes.
//    private FileDescription<FdResourcePath> inPipe;
//    private FileDescription<FdResourcePath> outPipe;
//    private FileDescription<FdResourcePath> errPipe;
//
//    private FileDescription<OutputStream> outputStream; // writes to the write end of the inPipe
//    private FileDescription<InputStream> inputStream; // reads from the read end of outPipe
//    private FileDescription<InputStream> errorStream; // read from the read end of errPipe

    protected void setupPipes(Path basePath) throws IOException {
        Path fd0 = basePath.resolve("fd0");
        Path fd1 = basePath.resolve("fd1");
        Path fd2 = basePath.resolve("fd2");

        PathLifeCycle lifeCycle = PathLifeCycles.deleteAfterExec(PathLifeCycles.namedPipe());
        PathResource rfd0 = new PathResource(fd0, lifeCycle);
        PathResource rfd1 = new PathResource(fd1, lifeCycle);
        PathResource rfd2 = new PathResource(fd2, lifeCycle);
        // outputStream = FileDescriptions.
    }

    Object exec(PGroup group) {
//        PB pb = group.createProcessBuilder();
//        // Wire up the process builder with the state of the group.
//        pb.redirectInput(context.in());
//        pb.redirectOutput(context.out());
//        pb.redirectError(context.in());

        return null;
    }
}


public class PGroup
    // implements PBF
    // implements ProcessBuilderFactory
{
    private static final Logger logger = LoggerFactory.getLogger(PGroup.class);

//    @Override
//    public IProcessBuilder<?> get() {
//        // TODO Auto-generated method stub
//        return null;
//    }

    public static Process wrap(ProcessCxt cxt, ProcessBuilder processBuilder) {
        Thread thread = new Thread(() -> {

            // processBuilder.redirectInput()
        });
        thread.start();

        return null;
    }

    public static Process exec(PExecCxt cxt, PBF pbf) {
        Process result = null;
        return result;
    }

    public static Redirect openInForNativeProcess(ProcessCxt cxt, int targetFd, JRedirect redirectIn) {
        Redirect result = (Redirect)redirectIn.accept(new JRedirectVisitor<Object>() {
            @Override
            public Object visit(JRedirectJava redirect) {

                return null;
            }

            @Override
            public Object visit(JRedirectFileDescription redirect) {
                return redirect.fileDescription();
            }

            @Override
            public Object visit(JRedirectIn redirect) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Object visit(JRedirectOut redirect) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Object visit(JRedirectPBF redirect) {
                // exec(cxt, redirect.bpf(), currentDepth, maxDepth, visited);
                exec(cxt, redirect.pbf());
                return null;
            }
        });
        return result;
    }

    public static Redirect openOutForNativeProcess(ProcessCxt cxt, int targetFd, JRedirect redirectIn) {
        Redirect result = (Redirect)redirectIn.accept(new JRedirectVisitor<Object>() {
            @Override
            public Object visit(JRedirectJava redirect) {

                return null;
            }

            @Override
            public Object visit(JRedirectFileDescription redirect) {
                return redirect.fileDescription();
            }

            @Override
            public Object visit(JRedirectIn redirect) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Object visit(JRedirectOut redirect) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Object visit(JRedirectPBF redirect) {
                // exec(cxt, redirect.bpf(), currentDepth, maxDepth, visited);
                exec(cxt, redirect.pbf());
                return null;
            }
        });
        return result;
    }

    public static void exec(ProcessCxt cxt, PBF pbf) {
        Set<PBF> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        int maxDepth = 128; // Maximum recursion depth
        exec(cxt, pbf, 0, maxDepth, visited);
    }

    public static void exec(ProcessCxt cxt, PBF pbf, int currentDepth, int maxDepth, Set<PBF> visited) {
        if (visited.contains(pbf)) {
            throw new RuntimeException("Cycle detected: " + pbf);
        }
        visited.add(pbf);

        PB pb = pbf.createProcessBuilder();
        Redirect redirectIn = openInForNativeProcess(cxt, maxDepth, pbf.redirectIn());
        Redirect redirectOut = openOutForNativeProcess(cxt, maxDepth, pbf.redirectOut());
        Redirect redirectErr = openOutForNativeProcess(cxt, maxDepth, pbf.redirectErr());

        // Recursively resolve file descriptions.
        FdTable<FdResource> fdTable = cxt.getFdTable();
        FdResource fdIn = fdTable.getResource(0);
        FdResource fdOut = fdTable.getResource(1);
        FdResource fdErr = fdTable.getResource(2);



        // - Every PIPE redirect has to go through a named pipe in order to be able to connect
        // java or native processes.
        // a process can be configured with 'write to pipe'


        // i think fdf always has to point to an open resource?
        // So all redirects are specs for FdResources.
        // or can it do open-on-demand?

        // So the options for input redirects are: discard, pipe, inherit, file-{read, write/append}.
        // but redirect is for process building - open process execution we need life resources.

        // fdr points to path of a named pipe.
        // fdr points to the path of a regular file.
        // fdr

        // pb.redirectError(cxt.ge);
        // Process process = pb.start();
    }

    public static void readLines(InputStream in, Consumer<String> lineCallback) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            br.lines().forEach(lineCallback::accept);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Thread readerThread(InputStream in, String prefix) {
        return new Thread(() -> {
            System.out.println("Reader thread created - prefix=" + prefix);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                br.lines().forEach(line -> System.out.println(prefix + line));
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                System.out.println("Reader thread terminated - prefix=" + prefix);
            }
        });
    }

    public static void main(String[] args) throws Exception {
        if (false) {
            ProcessBuilder pb = new ProcessBuilder("echo", "test");
            pb.redirectInput(Redirect.to(new File("/tmp/test")));
            pb.start();
        }

        Path basePath = Files.createTempDirectory("process-exec-");
        // logger.info("Created path at  " + basePath);
        System.out.println("Created path at  " + basePath);
        try (ProcessRunner ncxt = ProcessRunner.create(basePath, true, true, true)) {
            ncxt.setOutputReader(in -> readLines(in, line -> System.err.println(line)));
            ncxt.setErrorReader(in -> readLines(in, line -> System.err.println(line)));


//            readerThread(ncxt.getInputStream(), "out: ").start();
//            readerThread(ncxt.getErrorStream(), "err: ").start();


            if (true) {
                ncxt.setInputGenerator(out -> {
                    try (PrintStream pout = new PrintStream(out, false, StandardCharsets.UTF_8)) {
                        for (int i = 0; i < 1000; ++i) {
                            pout.println("" + i);
                        }
                    }
                }).start();
            }
            ProcessBuilder pb = ncxt.configure(new ProcessBuilder("head", "-n 2"));
            Process p = pb.start();

            if (false) {
                try (PrintStream pout = new PrintStream(ncxt.getOutputStream(), false, StandardCharsets.UTF_8)) {
                    for (int i = 0; i < 1000; ++i) {
                        pout.println("" + i);
                    }
                }

                if (false) {
                    try (InputStream xin = new ByteArrayInputStream("Hello\nWorld\n".getBytes(StandardCharsets.UTF_8))) {
                        try (OutputStream out = ncxt.getOutputStream()) {
                            xin.transferTo(ncxt.getOutputStream());
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            // Seems like we have to wait for the generating process/thread to finish?
            // writerThread.join();
            //ncxt.getOutputStream().close();

            int exitValue = p.waitFor();


            ProcessBuilder pb2 = ncxt.configure(new ProcessBuilder("head", "-n 2"));
            Process p2 = pb2.start();
            p2.waitFor();

            System.out.println("Process terminated with exit value " + exitValue);
        } finally {
            Files.deleteIfExists(basePath);
        }

        if (true ) {
            return;
        }


        PBF pbf = NativePBF.of("echo", "hi");

        // SysRuntimeImpl.forCurrentOs().createNamedPipe(null);

        // FileDescription.auto(new ByteArrayInputStream(new byte[]));
        FileDescription<FdResource> fd0 = FileDescriptions.of(new ByteArrayOutputStream());
        try (ProcessCxt cxt = new ProcessCxt()) {
            FdTable<FdResource> table = cxt.getFdTable();
            table.setFd(0, fd0);

            try (ProcessCxt cxt2 = cxt.dup()) {
                exec(cxt2, pbf);
                // cxt.exec();

            }
            System.out.println("is open: " + fd0.isOpen());
            // fd0.close();
        }
        System.out.println("is open: " + fd0.isOpen());
    }


//    @Override
//    public PB createProcessBuilder() {
//        // TODO Auto-generated method stub
//        return null;
//    }
}

