package org.aksw.vshell.registry;

import java.util.List;
import java.util.Set;

import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.exec.ListBuilder;
import org.aksw.shellgebra.exec.model.ExecSite;
import org.aksw.shellgebra.exec.model.ExecSiteDockerImage;
import org.aksw.shellgebra.exec.model.ExecSites;
import org.aksw.shellgebra.processbuilder.IProcessBuilderCore;
import org.aksw.shellgebra.processbuilder.ProcessBuilderDocker;
import org.aksw.shellgebra.shim.core.JvmCommandParser;

public class CmdOpVisitorToPbDocker
    extends CmdOpVisitorToBase {
    protected ExecSiteDockerImage execSite;

    public CmdOpVisitorToPbDocker(ExecSiteToProcessDispatcher dispatcher, ExecSiteDockerImage execSite) {
        super(dispatcher);
        this.execSite = execSite;
    }

    @Override
    protected IProcessBuilderCore<?> toProcessBuilder(List<String> args) {
        ExecSiteToProcessDispatcher dispatcher = getDispatcher();

        // ProcessRunner context = dispatcher.getContext();
        String commandName = args.get(0);
        // CommandParserCatalog parserCatalog = dispatcher.getParserCatalog();
        CommandCatalog commandCatalog = dispatcher.getCommandCatalog();
        JvmCommandRegistry commandRegistry = dispatcher.getContext().getJvmCmdRegistry();

        // Important:
        // (1) Parser candidates are inferred from the jvm site
        // (2) The actual command is resolved against the docker exec site.
        JvmCommandParser parser = null;
        Set<CommandBinding> parserCands = commandCatalog.get(commandName, ExecSites.jvm()).orElse(null);
        CommandBinding parserCand = null;
        if (parserCands != null) {
            for (CommandBinding cmdBinding : parserCands) {
                String cmdName = cmdBinding.commandName();
                parser = commandRegistry.get(cmdName).orElse(null);
                if (parser != null) {
                    parserCand = cmdBinding;
                    break;
                }
            }
        }

        if (parser == null) {
            throw new RuntimeException("No command parser found for: " + commandName);
        }

        // FIXME The actual command should re-use the prior resolution - probably need to bass the resolver or "probe results" tracker here.
        List<String> newArgv = resolveOrFail(commandCatalog, commandName, execSite, args);

        // List<String> newArgs = new ArrayList<>(args);
        // newArgs.set(0, actualCommandName);


//        JvmCommandParser parser = parserCatalog.getParser(commandName)
////        JvmCommandParser parser = context.getJvmCmdRegistry().get(commandName)
//            .orElseThrow(() -> new RuntimeException("No command parser found for: " + commandName));

        // TODO Resolve command name
        // getDispatcher().getContext().getJvmCmdRegistry().

        String imageRef = execSite.imageRef();
        FileMapper fileMapper = dispatcher.getFileMapper();

        // Issue: We need access to the Args model, especially readsStdin.
        // The CmdOp AST is not sufficient because it does not cover readsStdin (which is an interpretation of the args model).
        // The original command has been resolved, but the args parser was only linked to the original command.
        // Perhaps we can retain the original command - original command + exec site should
        // unambiguously give the actual command.

        IProcessBuilderCore<?> result = ProcessBuilderDocker.of(newArgv)
            .commandParser(parser)
            .imageRef(imageRef)
            .fileMapper(fileMapper)
            ;

        return result;
    }

    public static CommandBinding resolveOrFail(CommandCatalog commandCatalog, String commandName, ExecSite execSite) {
        // FIXME The actual command should re-use the prior resolution - probably need to bass the resolver or "probe results" tracker here.
        Set<CommandBinding> nameCands = commandCatalog.get(commandName, execSite)
            .orElseThrow(() -> new RuntimeException("command " + commandName + " not found on exec site " + execSite));
        if (nameCands.isEmpty()) {
            throw new RuntimeException("Command " + commandName + " does not have resolutions on exec site " + execSite);
        }
        CommandBinding resolvedName = nameCands.iterator().next();
        return resolvedName;
    }

    // Return resolved argv based on the given commandName and args.
    public static List<String> resolveOrFail(CommandCatalog commandCatalog, String commandName, ExecSite execSite, List<String> args) {
        CommandBinding commandBinding = resolveOrFail(commandCatalog, commandName, execSite);
        List<String> newArgs = commandBinding.argsTransform().map(args);
        List<String> newArgv = ListBuilder.ofString().add(commandBinding.commandName()).addAll(newArgs).buildList();
        return newArgv;
    }
}
