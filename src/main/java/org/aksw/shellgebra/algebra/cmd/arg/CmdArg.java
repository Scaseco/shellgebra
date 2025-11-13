package org.aksw.shellgebra.algebra.cmd.arg;

import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenCmdOp;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenLiteral;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenPath;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenVar;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.redirect.Redirect;

/**
 * Argument to a CmdOpExec.
 *
 * Path arguments may require quoting before they can be turned to the final strings.
 */
public interface CmdArg {
    <T> T accept(CmdArgVisitor<T> visitor);

    public static CmdArg redirect(Redirect redirect) {
        return new CmdArgRedirect(redirect);
    }

    public static CmdArg ofLiteral(String str) {
        return new CmdArgWord(StringEscapeType.ESCAPED, new TokenLiteral(str));
    }

    public static CmdArg ofString(String str) {
        return new CmdArgWord(StringEscapeType.SINGLE_QUOTED, new TokenLiteral(str));
    }

    public static CmdArg ofPathString(String pathStr) {
        return new CmdArgWord(StringEscapeType.SINGLE_QUOTED, new TokenPath(pathStr));
    }

    public static CmdArg ofVarName(String varName) {
        return new CmdArgWord(StringEscapeType.SINGLE_QUOTED, new TokenVar(varName));
    }

    public static CmdArg ofCommandSubstitution(CmdOp cmdOp) {
        return new CmdArgWord(StringEscapeType.SINGLE_QUOTED, new TokenCmdOp(cmdOp));
    }

    // Process substition such as <(cat /tmp/foo.txt).
    public static CmdArg ofProcessSubstution(CmdOp cmdOp) {
        return new CmdArgCmdOp(cmdOp);
    }
}
