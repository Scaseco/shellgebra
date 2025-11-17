package org.aksw.shellgebra.algebra.cmd.op;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.aksw.shellgebra.algebra.cmd.transformer.CmdOpTransformBase;
import org.aksw.shellgebra.algebra.cmd.transformer.CmdOpTransformer;

public class CmdOps {
    public static Set<CmdOpVar> accVars(CmdOp op) {
        return accVars(new LinkedHashSet<>(), op);
    }

    public static <C extends Collection<CmdOpVar>> C accVars(C result, CmdOp op) {
        Objects.requireNonNull(result);
        VarCollector vc = new VarCollector(result::add);
        CmdOpTransformer.transform(op, vc, null, null);
        return result;
    }

    public static CmdOp subst(CmdOp op, Function<CmdOpVar, CmdOp> replacement) {
        CmdOpTransformBase cmdOpTransform = new CmdOpTransformBase() {
            @Override
            public CmdOp transform(CmdOpVar op) {
                CmdOp r = replacement.apply(op);
                return r == null ? op : r;
            }
        };
        CmdOp result = CmdOpTransformer.transform(op, cmdOpTransform, null, null);
        return result;
    }

//    public static CmdOp subst(CmdOp op, Function<TokenVar, Token> varResolver) {
//        CmdOpTransform vc = new CmdOpTransformSubst(varResolver);
//        CmdOp result = CmdOpTransformer.transform(op, vc, null, null);
//        return result;
//    }

    /** Number of CmdOps in the given expression. 0 for null argument. */
    public static int size(CmdOp op) {
        if (op == null) {
            return 0;
        }

        int contrib = op.accept(new CmdOpVisitor<Integer>() {
            @Override
            public Integer visit(CmdOpExec op) {
                // TODO Visit vars in args?
                return 1;
            }

            @Override
            public Integer visit(CmdOpPipeline op) {
                return op.getSubOps().stream().map(CmdOps::size).reduce(0, (a, b) -> a + b);
            }

            @Override
            public Integer visit(CmdOpGroup op) {
                return op.subOps().stream().map(CmdOps::size).reduce(0, (a, b) -> a + b);
            }

            @Override
            public Integer visit(CmdOpVar op) {
                return 1;
            }
        });

        return 1 + contrib;
    }

    private static class VarCollector
        implements CmdOpTransformBase
    {
        private Consumer<CmdOpVar> acc;

        public VarCollector(Consumer<CmdOpVar> acc) {
            super();
            this.acc = Objects.requireNonNull(acc);
        }

        @Override
        public CmdOp transform(CmdOpVar op) {
            acc.accept(op);
            return CmdOpTransformBase.super.transform(op);
        }
    }
//
//    private static class CmdOpTransformSubst
//        extends CmdOpTransformBase
//    {
//        private Function<TokenVar, Token> tokenResolver;
//
//        public CmdOpTransformSubst(Function<TokenVar, Token> tokenResolver) {
//            super();
//            this.tokenResolver = Objects.requireNonNull(tokenResolver);
//        }
//
//        @Override
//        public CmdOp transform(CmdOp op) {
//            CmdOp result = varResolver.apply(op);
//            if (result == null) {
//                result = op;
//            }
//            return result;
//        }
//    }
}
