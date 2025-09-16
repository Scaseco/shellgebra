package org.aksw.shellgebra.algebra.cmd.op;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgLiteral;
import org.aksw.shellgebra.algebra.cmd.op.prefix.CmdPrefix;
import org.aksw.shellgebra.algebra.cmd.redirect.Redirect;

// List<Redirect> redirects
public record CmdOpExec(List<CmdPrefix> prefixes, String name, List<CmdArg> args, List<Redirect> redirects)
    implements CmdOp
{
//    public static CmdOpExec of(String... cmd) {
//        return of(Arrays.asList(cmd));
//        // return ofStrings(cmd[0], Arrays.copyOfRange(cmd, 1, cmd.length));
//    }

//    public static CmdOpExec of(List<String> cmd) {
//        return ofLiterals(cmd.get(0), cmd.subList(1, cmd.size()));
//    }

    /** Args array where the first element is the program name. */
    public static CmdOpExec ofLiteralArgs(String... args) {
        return ofLiterals(args[0], Arrays.asList(args).subList(1, args.length));
    }

    public static CmdOpExec ofLiterals(String name, String... args) {
        return ofLiterals(name, Arrays.asList(args));
    }

    public static CmdOpExec ofLiterals(String name, List<String> args) {
        return new CmdOpExec(List.of(), name, args.stream().<CmdArg>map(CmdArgLiteral::new).toList()); // , List.of());
    }

//    public static CmdOpExec ofLiterals(String name, List<String> args, List<Redirect> redirects) {
//        return new CmdOpExec(name, args.stream().<CmdArg>map(CmdArgLiteral::new).toList(), redirects);
//    }

    public CmdOpExec(String name, CmdArg ... args) {
        this(List.of(), name, List.of(args));
    }

    public CmdOpExec(String name, List<? extends CmdArg> args) {
        this(List.of(), name, List.<CmdArg>copyOf(args));
    }

    public CmdOpExec(String name, List<? extends CmdArg> args, List<? extends Redirect> redirects) {
        this(List.of(), name, List.<CmdArg>copyOf(args), List.<Redirect>copyOf(redirects));
    }

    public CmdOpExec(List<CmdPrefix> prefixes, String name, List<CmdArg> args) {
        this(prefixes, name, args, List.of());
    }

    // List<Redirect> redirects
    public CmdOpExec(List<CmdPrefix> prefixes, String name, List<CmdArg> args, List<Redirect> redirects) {
        // super();
        // super(args);
        this.prefixes = List.copyOf(prefixes);
        this.name = Objects.requireNonNull(name);
        this.args = List.<CmdArg>copyOf(args);
        this.redirects = List.<Redirect>copyOf(redirects);
    }

    public String getName() {
        return name;
    }

    public List<CmdArg> getArgs() {
        return args;
    }

    @Override
    public <T> T accept(CmdOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    @Override
    public String toString() {
        return "(exec " + "(" + getName() + ")" + CmdOp.toStrings(args) + ")";
    }
}
