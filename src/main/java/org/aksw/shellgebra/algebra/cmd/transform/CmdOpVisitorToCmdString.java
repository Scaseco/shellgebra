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
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVisitor;
import org.aksw.shellgebra.algebra.cmd.redirect.CmdRedirect;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectTargetVisitor;
import org.aksw.shellgebra.exec.CmdStrOps;


// Note: CmdString does not ensure that the string is actually a command -
// e.g. when passing a CmdOpFile it will return a string with the file name
// So if the result is expected to be an executable operation, then the check needs
// to be made beforehand by examining the type of the CmdOp.
public class CmdOpVisitorToCmdString
    implements CmdOpVisitor<CmdString>
{
    protected CmdStrOps strOps;

    public static String toString(CmdOp cmdOp, CmdOpVisitor<CmdString> visitor) {
        CmdString c = cmdOp.accept(visitor);
        String result = c.isCmd()
            ? Arrays.asList(c.cmd()).stream().collect(Collectors.joining(" "))
            : c.scriptString();
        return result;
    }

    public class CmdArgToString
        implements CmdArgVisitor<String> {
        @Override
        public String visit(CmdArgCmdOp arg) {
            String str = arg.cmdOp().accept(CmdOpVisitorToCmdString.this).scriptString();
            return strOps.processSubstitution(str);
        }

        @Override
        public String visit(CmdArgRedirect arg) {
            RedirectTargetVisitor<String> visitor = new RedirectTargetVisitorToString(strOps);
            String str = arg.redirect().toString(visitor);
            return str;
        }

        @Override
        public String visit(CmdArgWord arg) {
            CmdArgVisitor<String> visitor = new CmdArgVisitorRenderAsBashString(CmdOpVisitorToCmdString.this, strOps);
            String str = arg.accept(visitor);
            return str;
        }

    }

    public static String toArg(CmdString str) {
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
    public static String toArg(String[] cmd) {
        String result = Stream.of(cmd).collect(Collectors.joining(" "));
        return result;
    }

    public CmdOpVisitorToCmdString(CmdStrOps strOps) {
        super();
        this.strOps = Objects.requireNonNull(strOps);
    }

    @Override
    public CmdString visit(CmdOpExec op) {
        List<CmdArg> inArgs = op.args().args();
        CmdArgVisitor<String> visitor = new CmdArgVisitorRenderAsBashString(CmdOpVisitorToCmdString.this, strOps);
        List<String> outArgv = new ArrayList<>(inArgs.size() + 1);
        outArgv.add(op.name());
        for (CmdArg inArg : inArgs) {
            String str = inArg.accept(visitor);
            outArgv.add(str);
        }

        CmdString result;
        if (op.prefixes().isEmpty()) {
            result = new CmdString(outArgv.toArray(String[]::new));
        } else {
            StringBuilder sb = new StringBuilder();
            op.prefixes().forEach(e -> sb.append(e.key() + "=" + e.value()));
            sb.append(" ");
            sb.append(outArgv.stream().collect(Collectors.joining(" ")));
            result = new CmdString(sb.toString());
        }
        return result;
    }

    @Override
    public CmdString visit(CmdOpPipeline op) {
        List<String> args = op.getSubOps().stream().map(o -> o.accept(this)).map(CmdOpVisitorToCmdString::toArg).toList();
        CmdString result = new CmdString(strOps.pipeline(args));
        return result;
    }

    @Override
    public CmdString visit(CmdOpGroup op) {
        List<String> strs = CmdOpTransformLib.transformAll(this, op.subOps(), str -> CmdOpVisitorToCmdString.toArg(str));
        String str = strOps.group(strs);
        for (CmdRedirect redirect : op.redirects()) {
            str += " " + CmdRedirect.toString(strOps, redirect);
        }
        CmdString result = new CmdString(str);
        return result;
    }

    @Override
    public CmdString visit(CmdOpVar op) {
        throw new UnsupportedOperationException("Variable encountered: " + op);
    }
}
