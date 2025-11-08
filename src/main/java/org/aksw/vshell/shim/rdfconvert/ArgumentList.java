package org.aksw.vshell.shim.rdfconvert;

import java.util.List;
import java.util.Objects;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgRedirect;
import org.aksw.shellgebra.algebra.cmd.redirect.Redirect;

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

    public static ArgumentList of(CmdArg ... args) {
        return new ArgumentList(List.of(args));
    }

    public static ArgumentList of(List<CmdArg> args) {
        return new ArgumentList(List.copyOf(args));
    }

    public static ArgumentList ofLiterals(List<String> args) {
        List<CmdArg> tmp = args.stream().map(CmdArg::ofLiteral).map(x -> (CmdArg)x).toList();
        return new ArgumentList(tmp);
    }

    /** Get redirect arguments. */
    // Split into redirects and plain args (TODO cache on demand).
    public List<Redirect> getRedirects() {
        List<Redirect> result = args.stream()
            .map(arg -> arg instanceof CmdArgRedirect ca ? ca.redirect() : null)
            .filter(Objects::nonNull)
            .toList();
        return result;
    }

    /** Get non-redirect arguments. */
    public List<CmdArg> getRealArgs() {
        List<CmdArg> result = args.stream()
            .filter(arg -> !(arg instanceof CmdArgRedirect))
            .toList();
        return result;
    }
}
