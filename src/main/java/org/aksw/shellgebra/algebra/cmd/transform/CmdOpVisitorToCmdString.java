package org.aksw.shellgebra.algebra.cmd.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgCmdOp;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgRedirect;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgVisitor;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgVisitorRenderAsBashString;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgWord;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVisitor;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectVisitor;
import org.aksw.shellgebra.exec.CmdStrOps;


// Note: CmdString does not ensure that the string is actually a command -
// e.g. when passing a CmdOpFile it will return a string with the file name
// So if the result is expected to be an executable operation, then the check needs
// to be made beforehand by examining the type of the CmdOp.
public class CmdOpVisitorToCmdString
    implements CmdOpVisitor<CmdString>
{
    protected CmdStrOps strOps;


    public class CmdArgToString
        implements CmdArgVisitor<String> {
        @Override
        public String visit(CmdArgCmdOp arg) {
            String str = arg.cmdOp().accept(CmdOpVisitorToCmdString.this).scriptString();
            return "<(" + str + ")";
        }

        @Override
        public String visit(CmdArgRedirect arg) {
            RedirectVisitor<String> visitor = new RedirectVisitorToString(strOps);
            String str = arg.redirect().accept(visitor);
            return str;
            // String str = arg.cmdOp().accept(CmdOpVisitorToCmdString.this).scriptString();
            // return "<(" + str + ")";
        }

        @Override
        public String visit(CmdArgWord arg) {
            CmdArgVisitor<String> visitor = new CmdArgVisitorRenderAsBashString(strOps);
            String str = arg.accept(visitor);
            return str;
//        	String str = switch(arg.escapeType()) {
//        	case ESCAPED -> TokenVisitorRenderToString
//        	};
//            // TODO Auto-generated method stub
//            return null;
        }

    }

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
        List<CmdArg> inArgs = op.args().args();
        CmdArgVisitor<String> visitor = new CmdArgVisitorRenderAsBashString(strOps);
        List<String> outArgv = new ArrayList<>(inArgs.size() + 1);
        outArgv.add(op.name());
        for (CmdArg inArg : inArgs) {
            String str = inArg.accept(visitor);
            outArgv.add(str);
        }
        return new CmdString(outArgv.toArray(String[]::new));
        // String str = arg.accept(visitor);
        //return str;


//        // TODO Convert args to literals
//        // CmdString result = new CmdString(strOps.quoteArg(str));
//        List<CmdArg> args = op.getArgs();
//        List<String> argStrs = new ArrayList<>(1 + args.size());
//        argStrs.add(op.getName());
//        for (CmdArg arg : args) {
//            if (arg instanceof CmdArgCmdOp x) {
//                CmdString base = x.cmdOp().accept(this);
//                String str = toArg(base);
//                String result = strOps.quoteArg(str);
//                argStrs.add(result);
////                CmdString part = x.cmdOp().accept(this);
////                argStrs.addAll(Arrays.asList(part.cmd()));
//            } else if (arg instanceof CmdArgPath x) {
//                argStrs.add(strOps.quoteArg(x.path()));
//            } else if (arg instanceof CmdArgString x) {
//                argStrs.add(strOps.quoteArg(x.str()));
//            } else if (arg instanceof CmdArgLiteral x) {
//                argStrs.add(x.str());
//            } else if (arg instanceof CmdArgRedirect x) {
//                argStrs.add(RedirectVisitorToString.toString(strOps, x.redirect()));
//            } else {
//                throw new RuntimeException("shouldn't come here");
//            }
//        }
//
//        for (Redirect redirect : op.redirects()) {
//            argStrs.add(RedirectVisitorToString.toString(strOps, redirect));
//        }
//
//        // CmdOpTransformLib.transformAllArgs(argStrs, this, args, this::toArg);
//        CmdString result = new CmdString(argStrs.toArray(String[]::new));// strOps.call(op.getName(), argStrs);
//        return result;
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

//    @Override
//    public CmdString visit(CmdOpToArg op) {
//        String str = toArg(op.getSubOp().accept(this));
//        String result = strOps.quoteArg(str);
//        return new CmdString(result);
//    }

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
