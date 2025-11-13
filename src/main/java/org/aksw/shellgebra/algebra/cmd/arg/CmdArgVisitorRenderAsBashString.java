package org.aksw.shellgebra.algebra.cmd.arg;

import java.util.List;

import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenCmdOp;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenLiteral;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenPath;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenVar;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenVisitor;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVisitor;
import org.aksw.shellgebra.algebra.cmd.redirect.Redirect;
import org.aksw.shellgebra.algebra.cmd.transform.CmdOpVisitorToCmdString;
import org.aksw.shellgebra.algebra.cmd.transform.CmdString;
import org.aksw.shellgebra.algebra.cmd.transform.RedirectVisitorToString;
import org.aksw.shellgebra.exec.CmdStrOps;
import org.aksw.shellgebra.exec.CmdStrOpsBash;

public class CmdArgVisitorRenderAsBashString
    implements CmdArgVisitor<String>
{
    private CmdStrOps strOps;
    private CmdOpVisitor<CmdString> cmdOpVisitorToString;

    public CmdArgVisitorRenderAsBashString(CmdOpVisitor<CmdString> cmdOpVisitorToString, CmdStrOps strOps) {
        super();
        this.cmdOpVisitorToString = cmdOpVisitorToString;
        this.strOps = strOps;
    }

    public static List<String> render(List<CmdArg> args) {
        CmdStrOps strOps = CmdStrOpsBash.get();
        return render(new CmdOpVisitorToCmdString(strOps), strOps, args);
    }

    public static List<String> render(CmdOpVisitor<CmdString> cmdOpVisitorToString, CmdStrOps strOps, List<CmdArg> args) {
        CmdArgVisitor<String> renderer = new CmdArgVisitorRenderAsBashString(cmdOpVisitorToString, strOps);
        List<String> result = args.stream().map(arg -> arg.accept(renderer)).toList();
        return result;
    }

    @Override
    public String visit(CmdArgWord arg) {
        TokenVisitor<String> renderer = new TokenVisitorRenderToString();
        StringBuilder sb = new StringBuilder();
        for (Token token : arg.tokens()) {
            String contrib = token.accept(renderer);
            sb.append(contrib);
        }
        String result = sb.toString();
        return result;
    }

    // Process substitution
    @Override
    public String visit(CmdArgCmdOp arg) {
        CmdOp cmdOp = arg.cmdOp();
        String subStr = CmdOpVisitorToCmdString.toArg(cmdOp.accept(cmdOpVisitorToString));
        String result = strOps.processSubstitution(subStr);
        return result;
    }

    @Override
    public String visit(CmdArgRedirect arg) {
        Redirect redirect = arg.redirect();
        // The StrOps are used to escape filenames.
        String result = RedirectVisitorToString.toString(strOps, redirect);
        return result;
    }

    public class TokenVisitorRenderToString
        implements TokenVisitor<String>
    {
        @Override
        public String visit(TokenLiteral token) {
            return token.value();
        }

        @Override
        public String visit(TokenPath token) {
            return token.path();
        }

        @Override
        public String visit(TokenVar token) {
            return "$(" + token.name() + ")";
        }

        @Override
        public String visit(TokenCmdOp token) {
            CmdOp cmdOp = token.cmdOp();
            CmdString cmdString = cmdOp.accept(cmdOpVisitorToString);
            String result = CmdOpVisitorToCmdString.toArg(cmdString);
            return result;
        }
    }
}

