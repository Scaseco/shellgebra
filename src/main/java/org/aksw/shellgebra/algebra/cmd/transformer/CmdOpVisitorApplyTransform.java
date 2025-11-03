package org.aksw.shellgebra.algebra.cmd.transformer;

import java.util.ArrayList;
import java.util.List;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgCmdOp;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgLiteral;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgPath;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgRedirect;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgString;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgVisitor;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgWord;
import org.aksw.shellgebra.algebra.cmd.arg.Token;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenCmdOp;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenLiteral;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenPath;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenVar;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenVisitor;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVisitor;

public class CmdOpVisitorApplyTransform
    implements CmdOpVisitor<CmdOp>
{
    protected CmdOpTransform transform;

    public CmdOpVisitorApplyTransform(CmdOpTransform transform) {
        super();
        this.transform = transform;
    }

    public static List<CmdOp> transformAll(CmdOpVisitor<? extends CmdOp> transform, List<? extends CmdOp> subOps) {
        List<CmdOp> newOps = new ArrayList<>(subOps.size());
        for (CmdOp subOp : subOps) {
            CmdOp newOp = subOp.accept(transform);
            newOps.add(newOp);
        }
        return newOps;
    }

    public static List<CmdArg> transformAllArgs(CmdOpVisitor<? extends CmdOp> transform, List<? extends CmdArg> args) {
        TokenVisitor<Token> tokenVisitor = new TokenVisitor<>() {
            @Override
            public Token visit(TokenLiteral token) {
                return token;
            }

            @Override
            public Token visit(TokenPath token) {
                return token;
            }

            @Override
            public Token visit(TokenVar token) {
                return token;
            }

            @Override
            public Token visit(TokenCmdOp token) {
                CmdOp before = token.cmdOp();
                CmdOp after = before.accept(transform);
                return new TokenCmdOp(after);
            }
        };

        // Arg can be CmdArgWord or CmdArgRedirect.
        CmdArgVisitor<CmdArg> argVisitor = new CmdArgVisitor<>() {
            @Override
            public CmdArg visit(CmdArgWord arg) {
                List<Token> oldTokens = arg.tokens();
                List<Token> newTokens = new ArrayList<>(oldTokens.size());
                for (Token oldToken : oldTokens) {
                    Token newToken = oldToken.accept(tokenVisitor);
                    newTokens.add(newToken);
                }
                return new CmdArgWord(arg.escapeType(), newTokens);
            }

            @Override
            public CmdArg visit(CmdArgRedirect arg) {
                return arg;
            }

            @Override public CmdArg visit(CmdArgCmdOp arg)  {
                CmdOp before = arg.cmdOp();
                CmdOp after = before.accept(transform);
                return new CmdArgCmdOp(after);
            }

            @Override public CmdArg visit(CmdArgLiteral arg) { throw new UnsupportedOperationException(); }
            @Override public CmdArg visit(CmdArgString arg) { throw new UnsupportedOperationException(); }
            @Override public CmdArg visit(CmdArgPath arg) { throw new UnsupportedOperationException(); }
        };

        List<CmdArg> newArgs = new ArrayList<>(args.size());
        for (CmdArg oldArg : args) {
            CmdArg newArg = oldArg.accept(argVisitor);
//
//            CmdArg newArg = arg instanceof CmdArgCmdOp argOp
//                ? new CmdArgCmdOp(argOp.cmdOp().accept(transform))
//                : arg;
            newArgs.add(newArg);
        }
        return newArgs;
    }

    @Override
    public CmdOp visit(CmdOpExec op) {
        List<CmdArg> newOps = transformAllArgs(this, op.getArgs());
        CmdOp result = transform.transform(op, newOps);
        return result;
    }

    @Override
    public CmdOp visit(CmdOpPipeline op) {
//    	CmdOp subOp1 = op.getSubOp1();
//    	CmdOp subOp2 = op.getSubOp2();
//        CmdOp newOp1 = op.getSubOp1().accept(this);
//        CmdOp newOp2 = op.getSubOp2().accept(this);
        List<CmdOp> newOps = op.getSubOps().stream().map(subOp -> subOp.accept(this)).toList();
        CmdOp result = new CmdOpPipeline(newOps);
//        CmdOp result = transform.transform(op, newOp1, newOp2);
        return result;
    }

    @Override
    public CmdOp visit(CmdOpGroup op) {
        List<CmdOp> newOps = transformAll(this, op.subOps());
        CmdOp result = transform.transform(op, newOps);
        return result;
    }

//    @Override
//    public CmdOp visit(CmdOpString op) {
//        CmdOp result = transform.transform(op);
//        return result;
//    }

//    @Override
//    public CmdOp visit(CmdOpSubst op) {
//        CmdOp subOp = op.getSubOp().accept(this);
//        CmdOp result = transform.transform(op, subOp);
//        return result;
//    }

//    @Override
//    public CmdOp visit(CmdOpToArg op) {
//        CmdOp subOp = op.getSubOp().accept(this);
//        CmdOp result = transform.transform(op, subOp);
//        return result;
//    }

//    @Override
//    public CmdOp visit(CmdOpFile op) {
//        CmdOp result = transform.transform(op);
//        return result;
//    }

//    @Override
//    public CmdOp visit(CmdOpRedirectRight op) {
//        CmdOp newOp = op.getSubOp().accept(this);
//        CmdOp result = transform.transform(op, newOp);
//        return result;
//    }

    @Override
    public CmdOp visit(CmdOpVar op) {
        throw new UnsupportedOperationException();
    }
}
