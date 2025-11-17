package org.aksw.shellgebra.algebra.cmd.transformer;

import java.util.Objects;

import org.aksw.shellgebra.algebra.cmd.arg.Token;

public class TokenTransformer {

    public static Token transform(Token op, CmdTransformBase transform) {
        Objects.requireNonNull(transform);
        Token result = transform(op, transform, transform, transform);
        return result;
    }

    public static Token transform(Token op, TokenTransform tokenTransform, CmdOpTransform cmdOpTransform, CmdArgTransform cmdArgTransform) {
        if (tokenTransform == null) {
            tokenTransform = new TokenTransformBase() {};
        }

        if (cmdOpTransform == null) {
            cmdOpTransform = new CmdOpTransformBase() {};
        }

        if (cmdArgTransform == null) {
            cmdArgTransform = new CmdArgTransformBase() {};
        }

        TokenVisitorApplyTransform visitor = new TokenVisitorApplyTransform(tokenTransform, cmdOpTransform, cmdArgTransform);
        Token result = op.accept(visitor);
        return result;
    }
}
