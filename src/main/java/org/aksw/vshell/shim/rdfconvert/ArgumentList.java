package org.aksw.vshell.shim.rdfconvert;

import java.util.List;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgLiteral;

public record ArgumentList(List<CmdArg> args)
{
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
