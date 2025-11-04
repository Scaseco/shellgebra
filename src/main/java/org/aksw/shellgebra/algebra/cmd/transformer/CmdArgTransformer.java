package org.aksw.shellgebra.algebra.cmd.transformer;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;

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
}
