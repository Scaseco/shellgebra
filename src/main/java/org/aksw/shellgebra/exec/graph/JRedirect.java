package org.aksw.shellgebra.exec.graph;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;

import org.aksw.shellgebra.exec.IProcessBuilderCore;

// Low-level java redirect - PBF redirects have been resolved to plain input stream.
// However, the input stream to a process may be set directly.
// A java-native pseudo process may read directly from it - no java pipe pair necessary.
public sealed interface JRedirect {

    <T> T accept(JRedirectVisitor<T> visitor);

    // Standard Java redirect
    public record JRedirectJava(Redirect redirect) implements JRedirect {
        @Override public <T> T accept(JRedirectVisitor<T> visitor) {
            T result = visitor.visit(this);
            return result;
        }
    }

//    public record JRedirectProcessBuilder(IProcessBuilderCore<?> redirect) implements JRedirect {
//        @Override public <T> T accept(JRedirectVisitor<T> visitor) {
//            T result = visitor.visit(this);
//            return result;
//        }
//    }

    /** Anonymous pipes differ from regular files and named pipes in that they cannot be bind mounted. */
//    public record JRedirectAnonymousPipe(File file) implements JRedirect {
//        @Override public <T> T accept(JRedirectVisitor<T> visitor) {
//            T result = visitor.visit(this);
//            return result;
//        }
//    }

    // Standard Java redirect
    public record JRedirectFileDescription(FileDescription<FdResource> fileDescription) implements JRedirect {
        @Override public <T> T accept(JRedirectVisitor<T> visitor) {
            T result = visitor.visit(this);
            return result;
        }
    }

    public record JRedirectPBF(PBF pbf) implements JRedirect {
        @Override public <T> T accept(JRedirectVisitor<T> visitor) {
            T result = visitor.visit(this);
            return result;
        }
    }

    //public record PRedirectProcess(ByteSource in) implements JRedirect { }
    public record JRedirectIn(InputStream in) implements JRedirect {
        @Override public <T> T accept(JRedirectVisitor<T> visitor) {
            T result = visitor.visit(this);
            return result;
        }
    }

    public record JRedirectOut(OutputStream in) implements JRedirect {
        @Override public <T> T accept(JRedirectVisitor<T> visitor) {
            T result = visitor.visit(this);
            return result;
        }
    }
}
