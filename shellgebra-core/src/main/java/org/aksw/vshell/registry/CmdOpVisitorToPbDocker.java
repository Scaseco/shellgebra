package org.aksw.vshell.registry;

import java.util.List;
import java.util.Set;

import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.exec.model.ExecSiteDockerImage;
import org.aksw.shellgebra.exec.model.ExecSites;
import org.aksw.shellgebra.processbuilder.IProcessBuilderCore;
import org.aksw.shellgebra.processbuilder.ProcessBuilderDockerRun;
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
        CommandSiteCatalog commandCatalog = dispatcher.getCommandCatalog();
        JvmCommandRegistry commandRegistry = dispatcher.getJvmCmdRegistry();

        // FIXME The process below is probably wrong by now:
        //   There is an abstract command and this one has bindings to concrete commands.
        //   The binding may include an argument mapper that can validate whether the binding is applicable for the given arguments.
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

        // FIXME The actual command should re-use the prior resolution -
        // probably need to pass the resolver or "probe results" tracker here.
        List<String> newArgv = CommandCatalogs.resolveOrFail(commandCatalog, commandName, execSite, args);

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

        IProcessBuilderCore<?> result = ProcessBuilderDockerRun.of(newArgv)
            .commandParser(parser)
            .imageRef(imageRef)
            .fileMapper(fileMapper)
            ;

        return result;
    }
}
