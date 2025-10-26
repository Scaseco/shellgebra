package org.aksw.vshell.registry;

import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.shellgebra.exec.model.CommandLocation;

//@Deprecated // The cache alone is probably not that useful - CommandRegistryCache might be the better choice.
//public class CommandRegistryOverAvailability
//    implements CommandRegistry
//{
//    private CommandAvailability dynamicRegistry;
//
//    public CommandRegistryOverAvailability(CommandAvailability dynamicRegistry) {
//        super();
//        this.dynamicRegistry = dynamicRegistry;
//    }
//
//    @Override
//    public Set<CommandLocation> get(String virtualCommandName) {
//        Set<CommandLocation> matches =
//            dynamicRegistry.getKnownExecSites(virtualCommandName).stream().map(
//                execSite -> new CommandLocation(virtualCommandName, execSite))
//            .collect(Collectors.toUnmodifiableSet());
//        return matches;
//    }
//}
