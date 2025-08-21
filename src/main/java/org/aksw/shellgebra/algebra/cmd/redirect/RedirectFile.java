package org.aksw.shellgebra.algebra.cmd.redirect;

import java.util.Objects;

public record RedirectFile(String file, OpenMode openMode, int fd)
    implements Redirect
{
    // XXX Move to Redirect level?
    // "< file", "> file", ">> file", "<> file", ">| file"
    public enum OpenMode { READ, WRITE_TRUNCATE, WRITE_APPEND, READ_WRITE, CLOBBER }

    public RedirectFile {
        Objects.requireNonNull(file);
        Objects.requireNonNull(openMode);
    }

    public static RedirectFile fileFromStdIn(String file, OpenMode openMode) {
        return new RedirectFile(file, openMode, 0);
    }

    public static RedirectFile fileToStdOut(String file, OpenMode openMode) {
        return new RedirectFile(file, openMode, 1);
    }

    public static RedirectFile fileToStdErr(String file, OpenMode openMode) {
        return new RedirectFile(file, openMode, 2);
    }

    @Override
    public <T> T accept(RedirectVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
