package org.aksw.vshell.registry;

import java.util.List;

import org.aksw.commons.util.docker.Argv;
import org.aksw.shellgebra.exec.Stage;

public class JvmCommandWhich
    implements JvmCommand
{
    @Override
    public ArgsWhich parseArgs(String... args) {
        ArgsWhich model = ArgsWhich.parse(args).model();
        return model;
    }

    @Override
    public Stage newStage(String... args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int run(JvmExecCxt cxt, Argv argv) {
        ArgsWhich model;

        int exitValue = 0;
        try {
            model = parseArgs(argv.newArgs());
        } catch (Exception e) {
            e.printStackTrace(cxt.err());
            exitValue = 2;
            return exitValue;
        }

        List<String> pathEntries = PathResolutionUtils.getPathItems(cxt.env(), "PATH", ":");
        JvmCommandRegistry reg = cxt.context().getJvmCmdRegistry();
        for (String name : model.getFileNames()) {
            long limit = model.isAll() ? Long.MAX_VALUE : 1;
            List<String> res = resolve(reg, pathEntries, name, limit);

            if (res.isEmpty()) {
                exitValue = 1;
            }

            for (String str : res) {
                cxt.out().println(str);
            }
        }

        return exitValue;
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
