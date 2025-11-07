package org.aksw.shellgebra.exec;

import java.util.List;

public interface CmdStrOps {
    String subst(String str);

    /** Wrap a string in single quotes. */
    String quoteArg(String str);


    String escapeTokenNoQuote(String str);
    String escapeTokenSingleQuote(String str);
    String escapeTokenDoubleQuote(String str);

    String group(List<String> strs);
    // String pipe(String before, String after);
    String pipeline(List<String> parts);
    String call(String cmdName, List<String> args);
    String redirect(String cmd, String fileName);
    // exprEval $(...) -> Eval expression and substitute argument with result
}
