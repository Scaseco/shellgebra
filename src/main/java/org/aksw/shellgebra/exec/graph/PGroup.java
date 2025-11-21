package org.aksw.shellgebra.exec.graph;

import java.io.ByteArrayOutputStream;
import java.lang.ProcessBuilder.Redirect;

import com.google.common.io.ByteSource;

interface PBF {
    PB createProcessBuilder();
}

// High level redirect
sealed interface PRedirect {
    // Standard Java redirect
    public record PRedirectJava(Redirect redirect) implements PRedirect { }
    public record PRedirectProcess(PBF processBuilderFactory) implements PRedirect { }
}


// Low-level java redirect - PBF redirects have been resolved to plain input stream.
// However, the input stream to a process may be set directly.
// A java-native pseudo process may read directly from it - no java pipe pair necessary.
sealed interface JRedirect {
    // Standard Java redirect
    public record PRedirectJava(Redirect redirect) implements JRedirect { }
    public record PRedirectProcess(ByteSource in) implements JRedirect { }
}


class NativePBF
    implements PBF
{
    String[] command;

    @Override
    public PB createProcessBuilder() {
        // TODO Auto-generated method stub
        return null;
    }
}

class PProcessBuilderFactoryBase
    implements PBF
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


interface PB {
    void redirectInput(JRedirect redirect);
    void redirectOutput(JRedirect redirect);
    void redirectError(JRedirect redirect);

    Process start();
}


interface PExecCxt {
    JRedirect in();
    JRedirect out();
    JRedirect err();
}


class PExecutor {
    PExecCxt context; // These are life resources

    Object exec(PGroup group) {
        PB pb = group.createProcessBuilder();
        // Wire up the process builder with the state of the group.
        pb.redirectInput(context.in());
        pb.redirectOutput(context.out());
        pb.redirectError(context.in());

        return null;
    }
}


public class PGroup
    implements PBF
    // implements ProcessBuilderFactory
{
//    @Override
//    public IProcessBuilder<?> get() {
//        // TODO Auto-generated method stub
//        return null;
//    }


    public static void exec(ProcessCxt cxt, PBF pbf) {
        PB pb = pbf.createProcessBuilder();

        FdResource fdr = cxt.getFdTable().getResource(0);

        pb.redirectError(cxt.ge);
        Process process = pb.start();
    }

    public static void main(String[] args) {
        // FileDescription.auto(new ByteArrayInputStream(new byte[]));
        FileDescription<FdResource> fd0 = FileDescription.of(new ByteArrayOutputStream());
        try (ProcessCxt cxt = new ProcessCxt()) {
            FdTable<FdResource> table = cxt.getFdTable();
            table.setFd(0, fd0);

            try (ProcessCxt cxt2 = cxt.dup()) {

                // cxt.exec();


            }
            System.out.println("is open: " + fd0.isOpen());
            // fd0.close();
        }
        System.out.println("is open: " + fd0.isOpen());
    }


    @Override
    public PB createProcessBuilder() {
        // TODO Auto-generated method stub
        return null;
    }
}

