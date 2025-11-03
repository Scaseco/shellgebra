package org.aksw.shellgebra.algebra.cmd.op;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.aksw.shellgebra.algebra.cmd.arg.Token;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenVar;
import org.aksw.shellgebra.algebra.cmd.transformer.CmdOpTransform;
import org.aksw.shellgebra.algebra.cmd.transformer.CmdOpTransformBase;
import org.aksw.shellgebra.algebra.cmd.transformer.CmdOpTransformer;

public class CmdOps {
    public static Set<CmdOpVar> accVars(CmdOp op) {
        return accVars(new LinkedHashSet<>(), op);
    }

    public static <C extends Collection<CmdOpVar>> C accVars(C result, CmdOp op) {
        Objects.requireNonNull(result);
        VarCollector vc = new VarCollector(result::add);
        CmdOpTransformer.transform(op, vc);
        return result;
    }

    public static CmdOp subst(CmdOp op, Function<TokenVar, Token> varResolver) {
        CmdOpTransform vc = new CmdOpTransformSubst(varResolver);
        CmdOp result = CmdOpTransformer.transform(op, vc);
        return result;
    }

    private static class VarCollector
        extends CmdOpTransformBase
    {
        private Consumer<CmdOpVar> acc;

        public VarCollector(Consumer<CmdOpVar> acc) {
            super();
            this.acc = Objects.requireNonNull(acc);
        }

        @Override
        public CmdOp transform(CmdOpVar op) {
            acc.accept(op);
            return super.transform(op);
        }
    }

    private static class CmdOpTransformSubst
        extends CmdOpTransformBase
    {
        private Function<TokenVar, Token> tokenResolver;

        public CmdOpTransformSubst(Function<TokenVar, Token> tokenResolver) {
            super();
            this.tokenResolver = Objects.requireNonNull(tokenResolver);
        }

        @Override
        public CmdOp transform(CmdOp op) {
            CmdOp result = varResolver.apply(op);
            if (result == null) {
                result = op;
            }
            return result;
        }
    }
}
