package org.aksw.shellgebra.algebra.cmd.transformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgCmdOp;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgRedirect;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgVisitor;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgWord;
import org.aksw.shellgebra.algebra.cmd.arg.Token;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;

public interface CmdArgTransform {
    CmdArg transform(CmdArgWord arg, List<Token> subTokens);
    CmdArg transform(CmdArgCmdOp arg, CmdOp subOp);
    CmdArg transform(CmdArgRedirect arg);

    public static <T> List<T> transformArgs(CmdArgVisitor<T> visitor, List<CmdArg> args) {
        List<T> result = new ArrayList<>(args.size());
        for (CmdArg arg : args) {
            T value = arg.accept(visitor);
            result.add(value);
        }
        return Collections.unmodifiableList(result);
    }

    /** Util method to decouple visitor from instance. */
//    public static <T> T transform(CmdArgVisitor<T> visitor, CmdArg arg) {
//        T result = arg.accept(visitor);
//        return result;
//    }
}
