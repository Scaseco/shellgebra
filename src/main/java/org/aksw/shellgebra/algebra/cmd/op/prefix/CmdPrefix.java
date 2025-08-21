package org.aksw.shellgebra.algebra.cmd.op.prefix;

import java.util.Objects;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;

public sealed interface CmdPrefix {
    public record CmdPrefixEnvAssign(String key, String value) implements CmdPrefix {
        public CmdPrefixEnvAssign {
            Objects.requireNonNull(key);
            Objects.requireNonNull(value);
        }
    }

    public record CmdPrefixCmdOp(CmdOp cmdOp) implements CmdPrefix {
        public CmdPrefixCmdOp {
            Objects.requireNonNull(cmdOp);
        }
    }
}
