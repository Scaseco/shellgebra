package org.aksw.shellgebra.exec.graph;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;

// Low-level java redirect - PBF redirects have been resolved to plain input stream.
// However, the input stream to a process may be set directly.
// A java-native pseudo process may read directly from it - no java pipe pair necessary.
public sealed interface JRedirect {

    <T> T accept(JRedirectVisitor<T> visitor);

    // Standard Java redirect
    public record PRedirectJava(Redirect redirect) implements JRedirect {
        @Override public <T> T accept(JRedirectVisitor<T> visitor) {
            T result = visitor.visit(this);
            return result;
        }
    }

    // Standard Java redirect
    public record PRedirectFileDescription(FileDescription<FdResource> fileDescription) implements JRedirect {
        @Override public <T> T accept(JRedirectVisitor<T> visitor) {
            T result = visitor.visit(this);
            return result;
        }
    }

    public record PRedirectPBF(PBF pbf) implements JRedirect {
        @Override public <T> T accept(JRedirectVisitor<T> visitor) {
            T result = visitor.visit(this);
            return result;
        }
    }

    //public record PRedirectProcess(ByteSource in) implements JRedirect { }
    public record PRedirectIn(InputStream in) implements JRedirect {
        @Override public <T> T accept(JRedirectVisitor<T> visitor) {
            T result = visitor.visit(this);
            return result;
        }
    }

    public record PRedirectOut(OutputStream in) implements JRedirect {
        @Override public <T> T accept(JRedirectVisitor<T> visitor) {
            T result = visitor.visit(this);
            return result;
        }
    }
}
