package org.aksw.vshell.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import org.aksw.shellgebra.exec.model.ExecSite;
import org.aksw.shellgebra.shim.core.ArgsTransform;

/**
 * Registry / cache for whether a command is available on a certain exec site.
 * Should only be used with resolved commands such as /bin/foo
 * in contrast to names such as foo.
 */
public class CommandRegistry
    implements CommandCatalog
{
    // value may be null to indicate absence of the command.
    private Map<String, Multimap<ExecSite, CommandBinding>> toolToSiteToCmd = new HashMap<>();// HashBasedTable.create();

    /** Return a snapshot of the set of exec sites where the command is
     *  known to be present.
     *  Return null if there is no entry for the command.
     *  Empty map if there is an entry with no known locations.
     */
    public Optional<Multimap<ExecSite, CommandBinding>> getKnownExecSites(String command) {
        Multimap<ExecSite, CommandBinding> tmp = toolToSiteToCmd.get(command);
        Multimap<ExecSite, CommandBinding> result;
        if (tmp != null) {
            result = Multimaps.filterKeys(toolToSiteToCmd.get(command), k -> !tmp.get(k).isEmpty());
        } else {
            result = tmp;
        }
//        if (toolToSiteToCmd.containsRow(command)) {
//            tmp = toolToSiteToCmd.row(command).entrySet().stream()
//                .filter(Objects::nonNull)
//                .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue));
//        }
        return Optional.ofNullable(result);
    }

//    public Optional<Set<CommandLocation>> getKnownLocations(String command) {
//        return getKnownExecSites(command).map(m -> m.entrySet().stream()
//            .map(e -> new CommandLocation(e.getValue(), e.getKey()))
//            .collect(Collectors.toUnmodifiableSet()));
//    }

    public Optional<Set<CommandBinding>> getAvailability(String command, ExecSite execSite) {
        return Optional.ofNullable(toolToSiteToCmd.get(command)).map(mm -> (Set<CommandBinding>)mm.get(execSite));
    }

    public CommandRegistry put(String command, ExecSite execSite, String cmd) {
        put(command, execSite, cmd, ArgsTransform.identity());
        return this;
    }

    public CommandRegistry put(String command, ExecSite execSite, String cmd, ArgsTransform argsTransform) {
        if (argsTransform == null) {
            argsTransform = ArgsTransform.identity();
        }
        CommandBinding commandBinding = new CommandBinding(cmd, argsTransform);
        put(command, execSite, commandBinding);
        return this;
    }

    public CommandRegistry put(String command, ExecSite execSite, CommandBinding cmd) {
        toolToSiteToCmd.computeIfAbsent(command, c -> LinkedHashMultimap.create()).put(execSite, cmd);
        return this;
    }

    public CommandRegistry putAll(String command, Multimap<ExecSite, CommandBinding> cmdMap) {
        toolToSiteToCmd.computeIfAbsent(command, c -> LinkedHashMultimap.create()).putAll(cmdMap);
        return this;
    }

    @Override
    public Multimap<ExecSite, CommandBinding> get(String virtualCommandName) {
        return getKnownExecSites(virtualCommandName).orElse(Multimaps.unmodifiableMultimap(LinkedHashMultimap.create()));
    }
}
