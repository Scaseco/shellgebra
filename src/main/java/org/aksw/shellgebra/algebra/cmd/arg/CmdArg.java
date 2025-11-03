package org.aksw.shellgebra.algebra.cmd.arg;

import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenLiteral;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenPath;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenVar;

/**
 * Argument to a CmdOpExec.
 *
 * Path arguments may require quoting before they can be turned to the final strings.
 */
public interface CmdArg {
    <T> T accept(CmdArgVisitor<T> visitor);

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
}
