package org.aksw.shellgebra.algebra.cmd.transform;

import org.aksw.shellgebra.algebra.cmd.redirect.Redirect;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectFile;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectFile.OpenMode;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectVisitor;
import org.aksw.shellgebra.exec.CmdStrOps;

public class RedirectVisitorToString
    implements RedirectVisitor<String> {

    protected CmdStrOps strOps;

    public RedirectVisitorToString(CmdStrOps strOps) {
        super();
        this.strOps = strOps;
    }

    @Override
    public String visit(RedirectFile redirect) {
        OpenMode openMode = redirect.openMode();
        int fd = redirect.fd();
        String file = redirect.file();
        String arg = strOps.quoteArg(file);

        // XXX '> file' (notice the whitespace) would be nicer than '>file'.
        //   However, the current approach would incorrectly escape the whitespace when trying to quote the whole command.
        String result = switch (openMode) {
        case READ -> fdStr(fd, 0) + "<" + arg;
        case WRITE_TRUNCATE -> fdStr(fd, 1) + "> " + arg;
        case WRITE_APPEND -> fdStr(fd, 1) + ">>" + arg;
        case CLOBBER -> fdStr(fd, 1) + ">|" + arg;
        case READ_WRITE -> fdStr(fd, 1) + "<>" + arg;
        default -> throw new IllegalArgumentException("Unexpected value: " + openMode);
        };

        return result;
    }

    private static String fdStr(int requestedFd, int implicitFd) {
        return (requestedFd == implicitFd) ? "" : Integer.toString(requestedFd);
    }

    public static String toString(CmdStrOps strOps, Redirect redirect) {
        RedirectVisitor<String> visitor = new RedirectVisitorToString(strOps);
        String result = redirect.accept(visitor);
        return result;
    }
}
