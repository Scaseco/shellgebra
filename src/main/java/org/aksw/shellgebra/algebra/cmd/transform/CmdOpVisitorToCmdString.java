package org.aksw.shellgebra.algebra.cmd.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgCmdOp;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgLiteral;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgPath;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgRedirect;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgString;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpToArg;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVisitor;
import org.aksw.shellgebra.algebra.cmd.redirect.Redirect;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectFile;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectFile.OpenMode;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectVisitor;
import org.aksw.shellgebra.exec.CmdStrOps;


class RedirectVisitorToString
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

    public static String toString(CmdStrOps strOps, Redirect redirect) {
        RedirectVisitor<String> visitor = new RedirectVisitorToString(strOps);
        String result = redirect.accept(visitor);
        return result;
    }
}

// Note: CmdString does not ensure that the string is actually a command -
// e.g. when passing a CmdOpFile it will return a string with the file name
// So if the result is expected to be an executable operation, then the check needs
// to be made beforehand by examining the type of the CmdOp.
public class CmdOpVisitorToCmdString
    implements CmdOpVisitor<CmdString>
{
    protected CmdStrOps strOps;

//    public String toString(Redirect redirect) {
//        String result = switch (redirect) {
//        case RedirectFile r -> toString(r);
//        };
//        return result;
//    }

//    public String toString(RedirectFile redirect) {
//
//    }


    public String toArg(CmdString str) {
        String result = str.isScriptString()
            ? str.scriptString()
            : toArg(str.cmd()); // String.join(" ", str.cmd());
        return result;
    }

    /**
     * Convert a command array (including command name) into a script string.
     * The script string can be passed as an argument to the appropriate interpreted such
     * as [/bin/bash, -c, scriptString].
     * All arguments will be quoted as needed.
     */
    public String toArg(String[] cmd) {
        String result = Stream.concat(
            Stream.of(cmd[0]),
            Arrays.asList(cmd).subList(1, cmd.length).stream().map(strOps::quoteArg)
        ).collect(Collectors.joining(" "));
        return result;
    }

    public CmdOpVisitorToCmdString(CmdStrOps strOps) {
        super();
        this.strOps = Objects.requireNonNull(strOps);
    }

    @Override
    public CmdString visit(CmdOpExec op) {
        // TODO Convert args to literals
        // CmdString result = new CmdString(strOps.quoteArg(str));
        List<CmdArg> args = op.getArgs();
        List<String> argStrs = new ArrayList<>(1 + args.size());
        argStrs.add(op.getName());
        for (CmdArg arg : args) {
            if (arg instanceof CmdArgCmdOp x) {
                CmdString base = x.cmdOp().accept(this);
                String str = toArg(base);
                String result = strOps.quoteArg(str);
                argStrs.add(result);
//                CmdString part = x.cmdOp().accept(this);
//                argStrs.addAll(Arrays.asList(part.cmd()));
            } else if (arg instanceof CmdArgPath x) {
                argStrs.add(strOps.quoteArg(x.path()));
            } else if (arg instanceof CmdArgString x) {
                argStrs.add(strOps.quoteArg(x.str()));
            } else if (arg instanceof CmdArgLiteral x) {
                argStrs.add(x.str());
            } else if (arg instanceof CmdArgRedirect x) {
                argStrs.add(RedirectVisitorToString.toString(strOps, x.redirect()));
            } else {
                throw new RuntimeException("shouldn't come here");
            }
        }

        for (Redirect redirect : op.redirects()) {
            argStrs.add(RedirectVisitorToString.toString(strOps, redirect));
        }

        // CmdOpTransformLib.transformAllArgs(argStrs, this, args, this::toArg);
        CmdString result = new CmdString(argStrs.toArray(String[]::new));// strOps.call(op.getName(), argStrs);
        return result;
    }

    @Override
    public CmdString visit(CmdOpPipeline op) {
        List<String> args = op.getSubOps().stream().map(o -> o.accept(this)).map(this::toArg).toList();
        CmdString result = new CmdString(strOps.pipeline(args));
        return result;
    }

//    @Override
//    public CmdString visit(CmdOpPipe op) {
//        String before = toArg(op.getSubOp1().accept(this));
//        String after = toArg(op.getSubOp2().accept(this));
//        CmdString result = new CmdString(strOps.pipe(before, after));
//        return result;
//    }

    @Override
    public CmdString visit(CmdOpGroup op) {
        List<String> strs = CmdOpTransformLib.transformAll(this, op.subOps(), this::toArg);
        CmdString result = new CmdString(strOps.group(strs));
        return result;
    }

//    @Override
//    public CmdString visit(CmdOpSubst op) {
//        String str = toArg(op.getSubOp().accept(this));
//        CmdString result = new CmdString(strOps.subst(str));
//        return result;
//    }
//
//    @Override
//    public CmdString visit(CmdOpString op) {
//        return new CmdString(op.getValue());
//    }

    @Override
    public CmdString visit(CmdOpToArg op) {
        String str = toArg(op.getSubOp().accept(this));
        String result = strOps.quoteArg(str);
        return new CmdString(result);
    }

    /** For proper stringification file nodes of exec nodes need to be replaced with strings.
     *  See {@link CmdOpTransformArguments}
     */
//    @Override
//    public CmdString visit(CmdOpFile op) {
//        String str = op.getPath(); // op.getSubOp().accept(this);
//        CmdString result = new CmdString(strOps.quoteArg(str));
//        return result;
//    }

//    @Override
//    public CmdString visit(CmdOpRedirectRight op) {
//        String before = toArg(op.getSubOp().accept(this));
//        String fileName = op.getFileName();
//        CmdString result = new CmdString(strOps.redirect(before, fileName));
//        return result;
//    }

    @Override
    public CmdString visit(CmdOpVar op) {
        throw new UnsupportedOperationException();
    }
}
