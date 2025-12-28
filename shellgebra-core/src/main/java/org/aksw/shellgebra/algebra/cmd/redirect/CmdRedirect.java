package org.aksw.shellgebra.algebra.cmd.redirect;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectTarget.RedirectTargetFile;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectTarget.RedirectTargetProcessSubstitution;
import org.aksw.shellgebra.algebra.cmd.transform.RedirectTargetVisitorToString;
import org.aksw.shellgebra.exec.CmdStrOps;

public record CmdRedirect(int fd, OpenMode openMode, RedirectTarget target) {

    // XXX Move to Redirect level?
    // "< file", "> file", ">> file", "<> file", ">| file"
    public enum OpenMode { READ, WRITE_TRUNCATE, WRITE_APPEND, READ_WRITE, CLOBBER }


    public static CmdRedirect in(CmdOp cmdOp) {
        return new CmdRedirect(0, OpenMode.READ, new RedirectTargetProcessSubstitution(cmdOp));
    }

    public static CmdRedirect in(String fileName) {
        return new CmdRedirect(0, OpenMode.READ, new RedirectTargetFile(fileName));
    }

    public static CmdRedirect out(String fileName) {
        return new CmdRedirect(1, OpenMode.WRITE_TRUNCATE, new RedirectTargetFile(fileName));
    }

    public static CmdRedirect err(String fileName) {
        return new CmdRedirect(2, OpenMode.WRITE_TRUNCATE, new RedirectTargetFile(fileName));
    }

    // @Override
    public String toString(RedirectTargetVisitor<String> visitor) {
        String arg = target.accept(visitor);

        // XXX '> file' (notice the whitespace) would be nicer than '>file'.
        //   However, the current approach would incorrectly escape the whitespace when trying to quote the whole command.
        String result = switch (openMode) {
        case READ -> fdStr(fd, 0) + "<" + arg;
        case WRITE_TRUNCATE -> fdStr(fd, 1) + ">" + arg;
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

    public static String toString(CmdStrOps strOps, CmdRedirect redirect) {
        RedirectTargetVisitor<String> visitor = new RedirectTargetVisitorToString(strOps);
        // RedirectTargetVisitor<String> visitor = new RedirectTargetVisitorToString(new CmdOpVisitorToCmdString(CmdStrOpsBash.get()));
        String result = redirect.toString(visitor);
        return result;
    }
}
