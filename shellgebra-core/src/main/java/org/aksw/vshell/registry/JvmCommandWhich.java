package org.aksw.vshell.registry;

import java.util.List;

import org.aksw.vshell.shim.rdfconvert.ArgsModular;
import org.aksw.vshell.shim.rdfconvert.JvmCommandBase;
import org.apache.commons.exec.ExecuteException;

public class JvmCommandWhich
    extends JvmCommandBase<ArgsWhich>
{
    @Override
    public ArgsModular<ArgsWhich> parseArgs(String... args) {
        ArgsModular<ArgsWhich> result = ArgsWhich.parse(args);
        return result;
    }

    @Override
    public void runActual(JvmExecCxt cxt, ArgsWhich model) throws ExecuteException {
        List<String> pathEntries = PathResolutionUtils.getPathItems(cxt.env(), "PATH", ":");
        JvmCommandRegistry reg = cxt.getExecutor().getJvmCmdRegistry(); // cxt.getJvmCmdRegistry();
        int exitValue = 0;
        for (String name : model.getFileNames()) {
            long limit = model.isAll() ? Long.MAX_VALUE : 1;
            List<String> res = resolve(reg, pathEntries, name, limit);

            if (res.isEmpty()) {
                exitValue = 1;
            }

            for (String str : res) {
                cxt.out().printStream().println(str);
            }
        }

        if (exitValue != 0) {
            throw new ExecuteException("Failed to resolve file", exitValue);
        }
    }

//    public static List<String> resolve(JvmExecCxt cxt, String name, long max) {
//        List<String> pathEntries = PathResolutionUtils.getPathItems(cxt.env(), "PATH", ":");
//        JvmCommandRegistry reg = cxt.context().getJvmCmdRegistry();
//        List<String> res = resolve(reg, pathEntries, name, max);
//        return res;
//    }

    public static List<String> resolve(JvmCommandRegistry reg, List<String> pathEntries, String name, long max) {
        List<String> res = PathResolutionUtils.streamPathResolutionCandidates(pathEntries, name)
            .filter(n -> {
                boolean b = reg.get(n).isPresent();
                return b;
            })
            .limit(max)
            .toList();
        return res;
    }
}
