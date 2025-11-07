package org.aksw.shellgebra.algebra.cmd.arg;

import java.util.List;
import java.util.Objects;

import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenCmdOp;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenLiteral;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenPath;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenVar;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenVisitor;
import org.aksw.shellgebra.algebra.cmd.redirect.Redirect;
import org.aksw.shellgebra.algebra.cmd.transform.RedirectVisitorToString;
import org.aksw.shellgebra.exec.CmdStrOps;
import org.aksw.shellgebra.exec.CmdStrOpsBash;

public class CmdArgVisitorRenderAsBashString
    implements CmdArgVisitor<String>
{
    private CmdStrOps strOps;

    public CmdArgVisitorRenderAsBashString(CmdStrOps strOps) {
        super();
        this.strOps = strOps;
    }

    public static List<String> render(List<CmdArg> args) {
        return render(CmdStrOpsBash.get(), args);
    }

    public static List<String> render(CmdStrOps strOps, List<CmdArg> args) {
        CmdArgVisitor<String> renderer = new CmdArgVisitorRenderAsBashString(strOps);
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

    @Override
    public String visit(CmdArgCmdOp arg) {
        return arg.toString();
    }

    @Override
    public String visit(CmdArgRedirect arg) {
        Redirect redirect = arg.redirect();
        // The StrOps are used to escape filenames.
        String result = RedirectVisitorToString.toString(strOps, redirect);
        return result;
    }

    public static class TokenVisitorRenderToString
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
            return Objects.toString(token.cmdOp());
        }
    }
}

