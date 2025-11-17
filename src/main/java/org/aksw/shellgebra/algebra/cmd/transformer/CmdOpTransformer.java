package org.aksw.shellgebra.algebra.cmd.transformer;

import java.util.Objects;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;

public class CmdOpTransformer {

    public static CmdOp transform(CmdOp op, CmdTransformBase transform) {
        Objects.requireNonNull(transform);
        CmdOp result = transform(op, transform, transform, transform);
        return result;
    }

    public static CmdOp transform(CmdOp op, CmdOpTransform cmdOpTransform, CmdArgTransform cmdArgTransform, TokenTransform tokenTransform) {
        if (cmdArgTransform == null) {
            cmdArgTransform = new CmdArgTransformBase() {};
        }

        if (cmdOpTransform == null) {
            cmdOpTransform = new CmdOpTransformBase() {};
        }

        if (tokenTransform == null) {
            tokenTransform = new TokenTransformBase() {};
        }

        CmdOpVisitorApplyTransform visitor = new CmdOpVisitorApplyTransform(cmdOpTransform, cmdArgTransform, tokenTransform);
        CmdOp result = op.accept(visitor);
        return result;
    }
}
