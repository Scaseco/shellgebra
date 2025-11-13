package org.aksw.shellgebra.algebra.cmd.transformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.vshell.shim.rdfconvert.ArgumentList;

public class CmdArgTransformer {

    public static CmdArg transform(CmdArg arg, CmdArgTransform cmdArgTransform, CmdOpTransform cmdOpTransform, TokenTransform tokenTransform) {
        if (cmdArgTransform == null) {
            cmdArgTransform = new CmdArgTransformBase();
        }

        if (cmdOpTransform == null) {
            cmdOpTransform = new CmdOpTransformBase();
        }

        if (tokenTransform == null) {
            tokenTransform = new TokenTransformBase();
        }

        CmdArgVisitorApplyTransform visitor = new CmdArgVisitorApplyTransform(cmdArgTransform, cmdOpTransform, tokenTransform);
        CmdArg result = arg.accept(visitor);
        return result;
    }

    public static ArgumentList transform(ArgumentList args, CmdArgTransform cmdArgTransform, CmdOpTransform cmdOpTransform, TokenTransform tokenTransform) {
        List<CmdArg> list = transform(args.args(), cmdArgTransform, cmdOpTransform, tokenTransform);
        return new ArgumentList(list);
    }

    public static List<CmdArg> transform(List<? extends CmdArg> args, CmdArgTransform cmdArgTransform, CmdOpTransform cmdOpTransform, TokenTransform tokenTransform) {
        List<CmdArg> result = new ArrayList<>(args.size());
        for (CmdArg arg : args) {
            CmdArg outArg = transform(arg, cmdArgTransform, cmdOpTransform, tokenTransform);
            result.add(outArg);
        }
        return Collections.unmodifiableList(result);// List.copyOf(result);
    }
}
