package org.aksw.vshell.shim.rdfconvert;

import java.util.List;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgLiteral;

/**
 * A model that captures structured arguments. This record is used
 * by tool shims to represent the parsed arguments.
 * Arguments can be e.g. file paths or sub commands.
 * Note, that redirects as arguments should be avoided:
 * Redirects are handled by the shell and not by the command.
 */
public record ArgumentList(List<CmdArg> args) {
    public ArgumentList {
        args = List.copyOf(args);
    }

    public int size() {
        return args.size();
    }

    public static ArgumentList ofLiterals(List<String> args) {
        List<CmdArg> tmp = args.stream().map(CmdArgLiteral::new).map(x -> (CmdArg)x).toList();
        return new ArgumentList(tmp);
    }
}
