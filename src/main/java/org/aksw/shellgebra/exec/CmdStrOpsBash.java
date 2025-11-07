package org.aksw.shellgebra.exec;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CmdStrOpsBash
    implements CmdStrOps
{
    private static final CmdStrOpsBash instance = new CmdStrOpsBash();

    public static CmdStrOps get() {
        return instance;
    }

    @Override
    public String subst(String str) {
        return "<(" + str + ")";
    }

    @Override
    public String quoteArg(String cmd) {
        String result = cmd;

//        result = result
//            .replaceAll("\\{", "\\\\{")
//            .replaceAll("\\|", "\\\\|")
//            .replaceAll("\\}", "\\\\}");

        boolean containsSingleQuote = result.contains("'");
        if (containsSingleQuote) {
            // Escape single quotes
            result = "'" + result.replaceAll("\\'", "'\"'\"'") + "'"; // ' -> '"'"'
        } else {
            // Escape any white spaces
            result = result.replaceAll(" ", "\\\\ ");
        }

        return result;
    }

    @Override
    public String escapeTokenNoQuote(String token) {
        // Escape white spaces, newlines, quotes and double quotes.
        String result = token
            .replaceAll(" ", "\\\\ ")
            .replaceAll("'", "\\\\'")
            .replaceAll("\"", "\\\\\"")
            .replaceAll("\n", "\\\\\n");
        return result;
    }

    @Override
    public String escapeTokenSingleQuote(String token) {
        String result = token;

//      result = result
//          .replaceAll("\\{", "\\\\{")
//          .replaceAll("\\|", "\\\\|")
//          .replaceAll("\\}", "\\\\}");

        boolean containsSingleQuote = result.contains("'");
        if (containsSingleQuote) {
            // Escape single quotes
            result = result.replaceAll("\\'", "'\"'\"'"); // ' -> '"'"'
        } else {
            // Escape any white spaces
            result = result
                .replaceAll(" ", "\\\\ ")
                .replaceAll("\n", "\\\\\n");
        }

        return result;
    }

    @Override
    public String escapeTokenDoubleQuote(String token) {
        String result = token.replaceAll("\"", "\\\"");
        return result;
    }

    @Override
    public String group(List<String> strs) {
        // String result = "{ " + strs.stream().collect(Collectors.joining(" ; ")) + " }";
        String result = "{ " + strs.stream().map(x -> x + " ; ").collect(Collectors.joining()) + "}";
        return result;
    }

    @Override
    public String pipeline(List<String> strs) {
        // return before + " | " + after;
        return strs.stream().collect(Collectors.joining(" | "));
    }

    @Override
    public String call(String cmdName, List<String> args) {
        String result = Stream.concat(
                Stream.of(cmdName),
                args.stream())
            .collect(Collectors.joining(" "));
        return result;
    }

    @Override
    public String redirect(String cmdStr, String fileName) {
        return cmdStr + " > " + fileName;
    }
}
