package org.aksw.shellgebra.algebra.cmd.op.prefix;

import java.util.Objects;

public record CmdPrefix(String key, String value) {
    public CmdPrefix {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
    }
}

//public sealed interface CmdPrefix {
//    public record CmdPrefixEnvAssign(String key, String value) implements CmdPrefix {
//        public CmdPrefixEnvAssign {
//            Objects.requireNonNull(key);
//            Objects.requireNonNull(value);
//        }
//    }
//
//    public record CmdPrefixCmdOp(CmdOp cmdOp) implements CmdPrefix {
//        public CmdPrefixCmdOp {
//            Objects.requireNonNull(cmdOp);
//        }
//    }
//}
