package org.aksw.shellgebra.algebra.cmd.op;

import java.util.Arrays;
import java.util.List;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.op.prefix.CmdPrefix;
import org.aksw.vshell.shim.rdfconvert.ArgumentList;

// XXX Add a background flag
public record CmdOpExec(List<CmdPrefix> prefixes, String name, ArgumentList args) // , List<Redirect> redirects)
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
    public static CmdOpExec ofLiteralArgs(String... argv) {
        return ofLiteralArgs(List.of(argv));
    }

    public static CmdOpExec ofLiteralArgs(List<String> argv) {
        return ofLiterals(argv.get(0), argv.subList(1, argv.size()));
    }


    public static CmdOpExec assign(String key, String value) {
        return new CmdOpExec(List.of(new CmdPrefix(key, value)), null, ArgumentList.of());
    }

    /**
     * Create a CmdOpExec with all arguments interpreted as string literals.
     *
     * @param name
     * @param args
     * @return
     */
    public static CmdOpExec ofLiterals(String name, String... args) {
        return ofLiterals(name, Arrays.asList(args));
    }

    public static CmdOpExec ofLiterals(String name, List<String> args) {
        return new CmdOpExec(List.of(), name, ArgumentList.of(args.stream().<CmdArg>map(CmdArg::ofLiteral).toList())); // , List.of());
    }

//    public static CmdOpExec ofLiterals(String name, List<String> args, List<Redirect> redirects) {
//        return new CmdOpExec(name, args.stream().<CmdArg>map(CmdArgLiteral::new).toList(), redirects);
//    }

    public CmdOpExec(String name, CmdArg ... args) {
        this(List.of(), name, ArgumentList.of(args));
    }

    public CmdOpExec(String name, ArgumentList args) {
        this(List.of(), name, args);
    }

//    public CmdOpExec(String name, ArgumentList args) { //  List<? extends Redirect> redirects) {
//        this(List.of(), name, args); // List.<Redirect>copyOf(redirects));
//    }

//    public CmdOpExec(List<CmdPrefix> prefixes, String name, ArgumentList args) {
//        this(prefixes, name, args);
//    }

    // List<Redirect> redirects
    public CmdOpExec(List<CmdPrefix> prefixes, String name, ArgumentList args) { //, List<Redirect> redirects) {
        // super();
        // super(args);
        this.prefixes = List.copyOf(prefixes);
        // this.name = Objects.requireNonNull(name);
        this.name = name; // Allow null command to allow for simple assignment.
                          // TODO Either have different ctors or introduce a CmdOpAssign type.
        this.args = args;
        // this.redirects = List.<Redirect>copyOf(redirects);
    }

    public String getName() {
        return name;
    }

//    public List<CmdArg> getArgs() {
//        return args;
//    }

    @Override
    public <T> T accept(CmdOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    @Override
    public String toString() {
        return "(exec " + "(" + getName() + (args.size() == 0 ? "" : " ") + CmdOp.toStrings(args) + "))";
    }
}
