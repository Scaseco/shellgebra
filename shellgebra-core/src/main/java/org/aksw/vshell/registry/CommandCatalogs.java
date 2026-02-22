package org.aksw.vshell.registry;

import java.util.List;
import java.util.Set;

import org.aksw.shellgebra.exec.ListBuilder;
import org.aksw.shellgebra.exec.model.ExecSite;

public class CommandCatalogs {
    public static CommandBinding resolveOrFail(CommandSiteCatalog commandCatalog, String commandName, ExecSite execSite) {
        // FIXME The actual command should re-use the prior resolution - probably need to bass the resolver or "probe results" tracker here.
        Set<CommandBinding> nameCands = commandCatalog.get(commandName, execSite).orElse(null);
        // Sanity checks.
        if (nameCands == null) {
            throw new RuntimeException("command " + commandName + " not found on exec site " + execSite);
        } else if (nameCands.isEmpty()) {
            throw new RuntimeException("Command " + commandName + " does not have resolutions on exec site " + execSite);
        }
        CommandBinding resolvedName = nameCands.iterator().next();
        return resolvedName;
    }

    // Return resolved argv based on the given commandName and args.
    public static List<String> resolveOrFail(CommandSiteCatalog commandCatalog, String commandName, ExecSite execSite, List<String> args) {
        CommandBinding commandBinding = resolveOrFail(commandCatalog, commandName, execSite);
        List<String> newArgs = commandBinding.argsTransform().map(args);
        List<String> newArgv = ListBuilder.ofString().add(commandBinding.commandName()).addAll(newArgs).buildList();
        return newArgv;
    }

    public static List<String> resolveOrFail(CommandSiteCatalog commandCatalog, ExecSite execSite, List<String> argv) {
        String commandName = argv.get(0);
        List<String> rawArgs = argv.subList(1,  argv.size());
        List<String> newArgv = resolveOrFail(commandCatalog, commandName, execSite, rawArgs);
        return newArgv;
    }
}
