package org.aksw.shellgebra.exec;

import java.util.List;
import java.util.function.Function;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;

public class CommandRunnerWrapperBash<T>
    extends CommandRunnerWrapper<T>
{
    /**
     * Transform a command with shell invocation.
     * Typically just a ListTransformPrefix with e.g. ["/bin/bash", "-c"]
     */
    protected Function<List<String>, List<String>> shellCallTransform;

    public CommandRunnerWrapperBash(CommandRunner<T> delegate, List<String> prefix) {
        super(delegate);
        this.shellCallTransform = new ListTransformPrefix<>(prefix);
    }

    @Override
    public T call(String... argv) {
        CmdOp cmdOp = CmdOpExec.ofLiteralArgs(argv);
        String[] argArray = SysRuntimeImpl.forBash().compileCommand(cmdOp);
        List<String> argList = List.of(SysRuntimeImpl.quoteArg(SysRuntimeImpl.join(argArray)));
        List<String> newArgList = shellCallTransform.apply(argList);
        String[] newArgArray = newArgList.toArray(String[]::new);
        T result = super.call(newArgArray);
        return result;
    }
}
