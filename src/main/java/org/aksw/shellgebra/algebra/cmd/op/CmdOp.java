package org.aksw.shellgebra.algebra.cmd.op;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.redirect.CmdRedirect;
import org.aksw.vshell.shim.rdfconvert.ArgumentList;

public interface CmdOp {
    <T> T accept(CmdOpVisitor<T> visitor);

    default boolean isVar() {
        return false;
    }

    public static String toStrings(ArgumentList argList) {
        return toStrings(argList.args());
    }

    public static String toStrings(Collection<?> ...collections) {
        return Stream.of(collections).flatMap(Collection::stream).map(Object::toString).collect(Collectors.joining(" "));
    }

    public abstract class CmdOpVisitorModifyRedirect
        implements CmdOpVisitor<CmdOp>
    {
        protected List<CmdRedirect> additions;

        public CmdOpVisitorModifyRedirect(List<CmdRedirect> additions) {
            super();
            this.additions = additions;
        }

        @Override
        public CmdOp visit(CmdOpExec op) {
            List<CmdArg> newArgs = new ArrayList<>(op.args().args());
            for (CmdRedirect redirect : additions) {
                newArgs.add(CmdArg.redirect(redirect));
            }
            return new CmdOpExec(op.prefixes(), op.name(), ArgumentList.of(newArgs));
            // List<Redirect> finalRedirects = combine(op.redirects());
            // return new CmdOpExec(op.prefixes(), op.name(), op.args(), finalRedirects);
        }

        public List<CmdRedirect> combine(List<CmdRedirect> base) {
            List<CmdRedirect> result = Stream.concat(base.stream(), additions.stream()).toList();
            return result;
        }

        @Override
        public CmdOp visit(CmdOpGroup op) {
            return new CmdOpGroup(op.subOps(), combine(op.redirects()));
        }

        @Override
        public CmdOp visit(CmdOpVar op) {
            return op;
        }

//        @Override
//        public CmdOp visit(CmdOpToArg op) {
//            return op;
//        }
    }

    public class CmdOpVisitorAddRedirect
        extends CmdOpVisitorModifyRedirect
    {
        public CmdOpVisitorAddRedirect(List<CmdRedirect> redirects) {
            super(redirects);
        }

        @Override
        public CmdOp visit(CmdOpPipeline op) {
            List<CmdOp> subOps = op.getSubOps();
            CmdOp lastOp = subOps.get(subOps.size() - 1);
            CmdOp modifiedOp = lastOp.accept(this);
            List<CmdOp> list = Stream.concat(
                subOps.subList(0, subOps.size() - 1).stream(),
                Stream.of(modifiedOp)).toList();
            CmdOpPipeline result = new CmdOpPipeline(list);
            return result;
        }
    }

    public class CmdOpVisitorPrependRedirect
        extends CmdOpVisitorModifyRedirect
    {
        public CmdOpVisitorPrependRedirect(List<CmdRedirect> redirects) {
            super(redirects);
        }

        @Override
        public CmdOp visit(CmdOpPipeline op) {
            List<CmdOp> subOps = op.getSubOps();
            CmdOp firstOp = subOps.get(0);
            CmdOp modifiedOp = firstOp.accept(this);
            List<CmdOp> list = Stream.concat(
                Stream.of(modifiedOp),
                subOps.subList(0, subOps.size() - 1).stream()).toList();
            CmdOpPipeline result = new CmdOpPipeline(list);
            return result;
        }
    }
}
